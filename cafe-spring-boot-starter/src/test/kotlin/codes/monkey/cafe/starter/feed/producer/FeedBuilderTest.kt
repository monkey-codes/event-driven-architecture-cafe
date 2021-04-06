package codes.monkey.cafe.starter.feed.producer

import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class FeedBuilderTest {
    private val feedEntries =
            generateEntries()

    private fun generateEntries(range: IntRange = 0 until 2): List<FeedEntry> {
        return range.map {
            FeedEntry.create(
                entryId = it.toString(),
                sequenceNumber = it.toLong(),
                entityId = UUID.randomUUID().toString(),
                payloads = listOf(
                    Payload(version = 1, content = TestDataFactory.testEvent())
                )
            )
        }
    }

    @Test
    fun `it should use the baseUrl as the id for the feed`() {
        val baseHref = "http://localhost/feed"
        val feed = feedBuilder().build(baseHref,
                PageImpl(feedEntries, PageRequest.of(0, 2), 2))
        assertThat(feed.id).isEqualTo(baseHref)
    }

    @Test
    fun `it should use vendor specific mime types for content`() {
        val feed = feedBuilder().build("http://localhost/feed",
                PageImpl(feedEntries, PageRequest.of(0, 2), 2))
        assertThat(feed.entries.first().contents.first().type).isEqualTo("application/vnd.cafe.event-envelope+json")
    }

    @Test
    fun `it should provide event type information in title & summary`() {
        val feed = feedBuilder().build("http://localhost/feed",
                PageImpl(feedEntries, PageRequest.of(0, 2), 2))
        val entry = feed.entries.first()
        assertThat(entry.title).isEqualTo("TestEvent sequence: 1 entryId: ${entry.id} entityId: urn:uuid:${feedEntries.last().entityId}")
        assertThat(entry.summary.value).isEqualTo("TestEvent occurred on entity ${feedEntries.last().entityId}")
    }

    @Test
    fun `it should have an event type category`() {
        val feed = feedBuilder().build("http://localhost/feed",
            PageImpl(feedEntries, PageRequest.of(0, 2), 2))
        val entry = feed.entries.first()
        val category = entry.categories.first()
        assertThat(category.scheme).isEqualTo("http://cafe/events/categories/type")
        assertThat(category.term).isEqualTo(feedEntries.first().type)
    }

    @Test
    fun `it should add a self url to feed entry`() {
        val baseHref = "http://localhost/feed"
        val feed = feedBuilder().build(baseHref,
            PageImpl(feedEntries, PageRequest.of(0, 2), 2))
        val entry = feed.entries.first()
        val link = entry.alternateLinks.first()
        assertThat(link.href).isEqualTo("$baseHref-entry/${entry.id.removePrefix("urn:uuid:")}")
        assertThat(link.rel).isEqualTo("self")
    }

    @Test
    fun head() {
        val feed = feedBuilder().build("http://localhost/feed",
                PageImpl(listOf(), PageRequest.of(0, 2), 0))
        assertThat(feed.feedType).isEqualTo("atom_1.0")
        assertThat(feed.title).isEqualTo("Qwerty")
        assertThat(feed.authors[0].name).isEqualTo("Harry")
        assertThat(feed.updated.toInstant()).isEqualTo(Instant.EPOCH)
        assertThat(feed.otherLinks.find { it.rel == "current" }).isNull()
        assertThat(feed.otherLinks.find { it.rel == "self" }!!.href).isEqualTo("http://localhost/feed")
        assertThat(feed.otherLinks.find { it.rel == "next-archive" }).isNull()
        assertThat(feed.otherLinks.find { it.rel == "prev-archive" }).isNull()
    }

    @Test
    fun subscriptionDocument() {
        val feed = feedBuilder().build("http://localhost/feed",
                PageImpl(feedEntries, PageRequest.of(3, 2), 7))
        assertThat(feed.updated.toInstant().toEpochMilli()).isEqualTo(feedEntries[1].created.toInstant().toEpochMilli())
        assertThat(feed.otherLinks.find { it.rel == "current" }).isNull()
        assertThat(feed.otherLinks.find { it.rel == "self" }!!.href).isEqualTo("http://localhost/feed")
        assertThat(feed.otherLinks.find { it.rel == "via" }!!.href).isEqualTo("http://localhost/feed/3")
        assertThat(feed.otherLinks.find { it.rel == "next-archive" }).isNull()
        assertThat(feed.otherLinks.find { it.rel == "prev-archive" }!!.href).isEqualTo("http://localhost/feed/2")
    }

    @Test
    fun latestArchive() {
        val feed = feedBuilder().build("http://localhost/feed",
                PageImpl(feedEntries, PageRequest.of(2, 2), 7))
        assertThat(feed.updated.toInstant().toEpochMilli()).isEqualTo(feedEntries[1].created.toInstant().toEpochMilli())
        assertThat(feed.otherLinks.find { it.rel == "current" }!!.href).isEqualTo("http://localhost/feed")
        assertThat(feed.otherLinks.find { it.rel == "self" }!!.href).isEqualTo("http://localhost/feed/2")
        assertThat(feed.otherLinks.find { it.rel == "next-archive" }!!.href).isEqualTo("http://localhost/feed/3")
        assertThat(feed.otherLinks.find { it.rel == "prev-archive" }!!.href).isEqualTo("http://localhost/feed/1")
    }

    @Test
    fun middleArchive() {
        val feed = feedBuilder().build("http://localhost/feed", PageImpl(feedEntries, PageRequest.of(1, 2), 7))
        assertThat(feed.updated.toInstant().toEpochMilli()).isEqualTo(feedEntries[1].created.toInstant().toEpochMilli())
        assertThat(feed.otherLinks.find { it.rel == "current" }!!.href).isEqualTo("http://localhost/feed")
        assertThat(feed.otherLinks.find { it.rel == "self" }!!.href).isEqualTo("http://localhost/feed/1")
        assertThat(feed.otherLinks.find { it.rel == "next-archive" }!!.href).isEqualTo("http://localhost/feed/2")
        assertThat(feed.otherLinks.find { it.rel == "prev-archive" }!!.href).isEqualTo("http://localhost/feed/0")
    }

    @Test
    fun earliestArchive() {
        val feed = feedBuilder().build("http://localhost/feed", PageImpl(feedEntries, PageRequest.of(0, 2), 7))
        assertThat(feed.updated.toInstant().toEpochMilli()).isEqualTo(feedEntries[1].created.toInstant().toEpochMilli())
        assertThat(feed.otherLinks.find { it.rel == "current" }!!.href).isEqualTo("http://localhost/feed")
        assertThat(feed.otherLinks.find { it.rel == "self" }!!.href).isEqualTo("http://localhost/feed/0")
        assertThat(feed.otherLinks.find { it.rel == "next-archive" }!!.href).isEqualTo("http://localhost/feed/1")
        assertThat(feed.otherLinks.find { it.rel == "prev-archive" }).isNull()
    }

    private fun feedBuilder() = FeedBuilder(FeedConfig(2,
            "Qwerty",
            "Harry"))
}
