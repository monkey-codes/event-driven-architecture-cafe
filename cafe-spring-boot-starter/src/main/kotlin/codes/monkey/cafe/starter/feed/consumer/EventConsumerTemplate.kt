package codes.monkey.cafe.starter.feed.consumer

import com.rometools.rome.feed.atom.Entry
import com.rometools.rome.feed.atom.Link
import io.micrometer.core.instrument.MeterRegistry
import java.util.stream.Stream
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.client.RestOperations

abstract class EventConsumerTemplate(
    private val restTemplate: RestOperations,
    private val meterRegistry: MeterRegistry,
    vararg eventVersions: EventVersion
) : FeedListener {
    private val eventVersionSupport: EventVersionSupport = EventVersionSupport(*eventVersions)

    companion object {
        val LOGGER = LoggerFactory.getLogger(EventConsumerTemplate::class.java)!!
    }

    override fun handle(stream: Stream<Entry>, progressTracker: ProgressTracker) {
        val isSupportedEventTypeFunc = eventVersionSupport.supportedEventTypes()
        val mapToSupportedEventVersionFunc = eventVersionSupport.toSupportedEventVersion(restTemplate)
        val consumeFunc = eventVersionSupport.consume(meterRegistry, this::consume)

        stream
            .forEach {
                try {
                    MDC.put("entryId", it.id)
                    it.categories.firstOrNull { it.scheme == "http://cafe/events/categories/type" }
                        ?.let {
                            MDC.put("publicEventType", it.term)
                    }
                    val isSupportedEventType = isSupportedEventTypeFunc(it)
                    if (isSupportedEventType) {
                        val mappedEntry = mapToSupportedEventVersionFunc(it)
                        consumeFunc(mappedEntry)
                        LOGGER.debug("Entry {} consumed successfully.", it.id)
                    } else {
                        LOGGER.debug("Entry {} is not a supported event type. Ignoring.", it.id)
                    }
                } catch (e: Exception) {
                    LOGGER.error("Entry {} consumption failed: {}", it.id, e.message, e)
                    throw e
                } finally {
                    MDC.remove("entryId")
                    MDC.remove("publicEventType")
                }

                val entryPageURL = extractEntryPageUrl(it)
                progressTracker.lastProcessed(it.id, entryPageURL)
            }
    }

    private fun extractEntryPageUrl(entry: Entry): String? {
        if (entry.source == null) {
            LOGGER.warn("Entry {} has no source property. We can't record the page on which we saw it.", entry.id)
            return null
        }

        val links = listOf<Link>()
            .union(entry.source.otherLinks ?: listOf())
            .union(entry.source.alternateLinks ?: listOf())

        val entryPageUrl = links.firstOrNull { it.rel == "via" }?.href
            ?: links.firstOrNull { it.rel == "self" }?.href

        if (entryPageUrl == null) {
            LOGGER.warn("Entry {}'s source has no via or self link. We can't record the page on which we saw it.", entry.id)
            return null
        }

        LOGGER.debug("Entry {} has page URL {}", entry.id, entryPageUrl)
        return entryPageUrl
    }

    abstract fun consume(entryMapping: EntryMapping)
}
