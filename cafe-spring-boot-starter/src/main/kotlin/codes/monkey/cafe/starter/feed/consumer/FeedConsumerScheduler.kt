package codes.monkey.cafe.starter.feed.consumer

import codes.monkey.cafe.starter.feed.consumer.ConsumptionStatus.ERROR
import codes.monkey.cafe.starter.feed.consumer.ConsumptionStatus.SUCCESS
import codes.monkey.cafe.starter.leaderelection.LeadershipContext
import codes.monkey.cafe.starter.leaderelection.LeadershipListener
import com.rometools.rome.feed.atom.Entry
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Stream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger

interface ProgressTracker {
    fun lastProcessed(entryId: String, entryPageURL: String?)
}

interface FeedListener {
    fun handle(stream: Stream<Entry>, progressTracker: ProgressTracker)
}

interface StreamPreProcessor {
    fun process(url: String, stream: Stream<Entry>): Stream<Entry>
}

@ConfigurationProperties(prefix = "atomfeed.client")
open class FeedConsumerScheduler(
        @Autowired val feedConsumer: FeedConsumer,
        val feedPositionRepository: FeedPositionRepository,
        val taskScheduler: TaskScheduler,
        val feedListener: FeedListener,
        val meterRegistry: MeterRegistry,
        val streamPreProcessors: List<StreamPreProcessor>
) : ApplicationListener<ContextRefreshedEvent> {

    companion object {
        val LOGGER = LoggerFactory.getLogger(FeedConsumerScheduler::class.java)
    }

    var schedules: List<Schedule>? = null

    private val shedulesConfigured = AtomicBoolean(false)

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (shedulesConfigured.compareAndSet(false, true)) {
            LOGGER.info("Found scheduled jobs for ${schedules?.size}")
            schedules?.forEach {
                taskScheduler.schedule(runnableFactory(it), CronTrigger(it.cron!!))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // Ignore: Generic handler to make sure timing recorded
    protected open fun runnableFactory(it: Schedule): Runnable {
        return Runnable {

            val startTime = System.nanoTime()
            val url = it.url!!

            try {
                LOGGER.info("Triggered feed pull for {}", url)

                LOGGER.debug("Streaming events from {} to {}", url, feedListener::class.java.simpleName)

                val feedPosition = feedPositionRepository.findFirstByUrl(url).orElse(FeedPosition(url = url, updated = OffsetDateTime.now()))

                val events: Stream<Entry> = when (feedPosition.lastProcessedEntryId) {
                    null -> feedConsumer.streamAll(url)
                    else -> eventsAfter(url, feedPosition)
                }.peek { entry ->
                    LOGGER.debug("Reading {} from {}", entry.title, url)
                }

                val preprocessedEvents = streamPreProcessors
                    .fold(events) { stream, preProcessor -> preProcessor.process(url, stream) }

                feedListener.handle(preprocessedEvents, object : ProgressTracker {
                    override fun lastProcessed(entryId: String, entryPageURL: String?) {
                        LOGGER.debug("Recording last feed position as entry ID {} on page URL {}", entryId, entryPageURL)
                        feedPosition.lastProcessedEntryId = entryId
                        feedPosition.lastProcessedEntryPageUrl = entryPageURL
                        feedPosition.updated = OffsetDateTime.now()
                        feedPositionRepository.save(feedPosition)
                    }
                })
                LOGGER.debug("{} finished consuming events from {}", feedListener::class.java.simpleName, url)
                timerBuilder(url, SUCCESS)
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            } catch (e: Exception) {
                timerBuilder(url, ERROR)
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                LOGGER.warn("Failed to consume feed from {}", url)
//                throw e
            }

            LOGGER.info("Completed feed pull for {}", url)
        }
    }

    private fun eventsAfter(defaultURL: String, feedPosition: FeedPosition): Stream<Entry> {
        val url: String = if (feedPosition.lastProcessedEntryPageUrl.isNullOrBlank()) defaultURL else feedPosition.lastProcessedEntryPageUrl!!
        return try {
            feedConsumer.eventsAfter(url, feedPosition.lastProcessedEntryId!!)
        } catch (e: EntryNotFoundException) {
            LOGGER.warn("Entry {} not found from last read page {}, falling back to current url",
                feedPosition.lastProcessedEntryId, feedPosition.lastProcessedEntryPageUrl)
            feedConsumer.eventsAfter(defaultURL, feedPosition.lastProcessedEntryId!!)
        }
    }

    private fun timerBuilder(
        url: String,
        status: ConsumptionStatus
    ) = Timer.builder("atom.feed.scheduling")
        .tags(listOf(
            Tag.of("url", url),
            Tag.of("status", status.name)
        ))
        .description("Scheduling events for atom feed")
}

class LeaderOnlyFeedConsumerScheduler(
        feedConsumer: FeedConsumer,
        feedPositionRepository: FeedPositionRepository,
        taskScheduler: TaskScheduler,
        feedListener: FeedListener,
        meterRegistry: MeterRegistry,
        streamPreProcessors: List<StreamPreProcessor>
) : FeedConsumerScheduler(feedConsumer, feedPositionRepository, taskScheduler, feedListener, meterRegistry, streamPreProcessors), LeadershipListener {

    private val isLeader = AtomicBoolean(false)

    override fun onLeadershipGranted(context: LeadershipContext) {
        isLeader.set(true)
    }

    override fun onLeadershipRevoked(context: LeadershipContext) {
        isLeader.set(false)
    }

    override fun runnableFactory(it: Schedule): Runnable {
        return Runnable {
            if (isLeader.get()) {
                LOGGER.debug("Executing scheduled pull of ${it.url} on ${InetAddress.getLocalHost().hostAddress}")
                super.runnableFactory(it).run()
            } else {
                LOGGER.debug("Not the leader, ignoring scheduled pull of ${it.url}")
            }
        }
    }
}

data class Schedule(
    var cron: String? = null,
    var url: String? = null
)
