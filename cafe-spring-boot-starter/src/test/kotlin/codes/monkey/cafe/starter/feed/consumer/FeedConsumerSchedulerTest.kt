package codes.monkey.cafe.starter.feed.consumer

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import java.util.concurrent.ScheduledFuture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger

@ExtendWith(MockKExtension::class)
internal class FeedConsumerSchedulerTest {

    @RelaxedMockK
    lateinit var feedConsumer: FeedConsumer
    @RelaxedMockK
    lateinit var feedPositionRepository: FeedPositionRepository
    @RelaxedMockK
    lateinit var taskScheduler: TaskScheduler
    @RelaxedMockK
    lateinit var feedListener: FeedListener
    @RelaxedMockK
    lateinit var meterRegistry: MeterRegistry
    var streamPreProcessors: List<StreamPreProcessor> = emptyList()

    lateinit var feedConsumerScheduler: FeedConsumerScheduler
    private val url = "https://localhost/feed"

    @BeforeEach
    fun setup() {
        feedConsumerScheduler = FeedConsumerScheduler(
            feedConsumer,
            feedPositionRepository,
            taskScheduler,
            feedListener,
            meterRegistry,
            streamPreProcessors
        )
        feedConsumerScheduler.schedules = listOf(Schedule(cron = "* * * * * *", url = url))
    }

    @Test
    fun `it should fallback to current url if it cannot find last processed entry from last read page`() {
        var runnable: Runnable? = null
        every { taskScheduler.schedule(any<Runnable>(), any<Trigger>()) } answers {
            runnable = this.firstArg()
            mockk<ScheduledFuture<Any>>()
        }
        val lastProcessedEntryPageUrl = "https://localhost/feed/1"
        val lastProcessedEntryId = "1"
        every { feedPositionRepository.findFirstByUrl(any()) } returns Optional.of(FeedPosition(
            lastProcessedEntryId = lastProcessedEntryId,
            lastProcessedEntryPageUrl = lastProcessedEntryPageUrl,
            url = url
        ))
        every { feedConsumer.eventsAfter(lastProcessedEntryPageUrl, lastProcessedEntryId) } throws
            EntryNotFoundException("not found", lastProcessedEntryId, lastProcessedEntryPageUrl)

        feedConsumerScheduler.onApplicationEvent(ContextRefreshedEvent(mockk()))
        runnable!!.run()

        verify(exactly = 1) { feedConsumer.eventsAfter(url, lastProcessedEntryId) }
    }
}
