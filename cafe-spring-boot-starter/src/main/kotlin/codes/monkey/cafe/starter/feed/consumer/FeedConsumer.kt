package codes.monkey.cafe.starter.feed.consumer

import codes.monkey.cafe.starter.feed.consumer.Direction.BACKWARD
import codes.monkey.cafe.starter.feed.consumer.Direction.FORWARD
import com.rometools.rome.feed.atom.Entry
import com.rometools.rome.feed.atom.Feed
import com.rometools.rome.io.WireFeedInput
import com.rometools.rome.io.XmlReader
import java.util.LinkedList
import java.util.Queue
import java.util.Spliterator
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
class FeedConsumer(private val restOperations: RestOperations) {

    private fun streamFrom(currentUrl: String, direction: Direction): Stream<LocationAwareEntry> =
        StreamSupport.stream(FeedSpliterator(currentUrl = currentUrl, direction = direction, restOperations = restOperations), false)

    fun streamAll(url: String): Stream<Entry> =
        streamFrom(url, BACKWARD)
            .reduce { _, second -> second }
            .let {
                if (!it.isPresent) return Stream.empty()
                streamFrom(it.get().url, FORWARD)
                    .map { it.entry }
            }

    fun eventsAfter(url: String, lastProcessedEntryId: String): Stream<Entry> =
        streamFrom(url, BACKWARD)
            .filter { it.entry.id == lastProcessedEntryId }
            .findFirst()
            .let {
                it.orElseThrow { EntryNotFoundException(
                    message = "Last processed entryId $lastProcessedEntryId not found in feed",
                    entryId = lastProcessedEntryId,
                    url = url
                ) }
                streamFrom(it.get().url, FORWARD)
                    .filter(FilterUntil<LocationAwareEntry>(Predicate { it.entry.id == lastProcessedEntryId }))
                    .map { it.entry }
            }
}

enum class Direction(val nextPageUrl: (Feed) -> String?, val sort: (List<Entry>) -> List<Entry> = { it }) {
    FORWARD(
        nextPageUrl = { feed ->
            feed.otherLinks?.find { it.rel == "next-archive" }?.href
        },
        sort = { it.reversed() }
    ),
    BACKWARD(
        nextPageUrl = { feed -> feed.otherLinks?.find { it.rel == "prev-archive" }?.href }
    )
}

internal class FeedSpliterator(
        val currentUrl: String,
        val direction: Direction,
        val restOperations: RestOperations
) : Spliterator<LocationAwareEntry> {

    private val queue: Queue<LocationAwareEntry> = LinkedList()
    private var nextPageUrl: String? = currentUrl

    companion object {
        val LOGGER = LoggerFactory.getLogger(FeedSpliterator::class.java)!!
    }

    override fun estimateSize(): Long = Long.MAX_VALUE

    override fun characteristics(): Int = Spliterator.ORDERED or
        Spliterator.DISTINCT or Spliterator.NONNULL or Spliterator.IMMUTABLE

    internal fun fetchFeed(url: String): Feed {
        val s = restOperations.getForObject(url, String::class.java)!!
        val byteArrayInputStream = s.byteInputStream()
        val wireFeed = WireFeedInput().build(XmlReader(byteArrayInputStream))
        val feed = wireFeed as Feed

        // Feeds returned via WireFeed() have entries that don't appear to have their .source property set.
        feed.entries.forEach { it.source = feed }

        return feed
    }

    override fun tryAdvance(action: Consumer<in LocationAwareEntry>?): Boolean {
        enqueueIfRequired()
        if (queue.isEmpty()) return false

        action?.accept(queue.remove())
        return true
    }

    private fun enqueueIfRequired() {
        queue.takeIf { it.isEmpty() }?.also { queue ->
            LOGGER.debug("Queueing up events from $nextPageUrl ")
            nextPageUrl = nextPageUrl?.let { url ->
                fetchFeed(url).let { feed ->
                    LOGGER.debug("Queued up ${feed.entries.size} event entries from $url")
                    queue.addAll(direction.sort(feed.entries).map { LocationAwareEntry(it, url) })
                    direction.nextPageUrl(feed)
                }
            }
            LOGGER.debug("Next page is $nextPageUrl")
        }
        if (queue.isEmpty() && nextPageUrl != null) enqueueIfRequired()
    }

    override fun trySplit(): Spliterator<LocationAwareEntry>? = null
}

private class FilterUntil<T>(val delegate: Predicate<T>) : Predicate<T> {
    var found = false
    override fun test(t: T): Boolean {
        return found || when (delegate.test(t)) {
            true -> {
                found = true
                return false
            }
            else -> false
        }
    }
}

data class LocationAwareEntry(val entry: Entry, val url: String)
data class EntryNotFoundException(override val message: String, val entryId: String, val url: String) : Exception()
