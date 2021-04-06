package codes.monkey.cafe.starter.feed.consumer

import assertk.assertThat
import assertk.assertions.*
import assertk.catch
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.util.stream.Collectors
import kotlin.streams.toList
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestOperations

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
internal class FeedConsumerTest {

    @RelaxedMockK
    lateinit var restOperations: RestOperations

    lateinit var feedConsumer: FeedConsumer

    @BeforeEach
    fun setup() {
        clearAllMocks()
        feedConsumer = FeedConsumer(restOperations)
        // Current or Recent Feed
        every { restOperations.getForObject("http://localhost:8082/feed", String::class.java) } returns feedPage(
            prevArchive = "/2",
            via = "/3",
            entryRange = 9 downTo 9
        )
        // Working Archive, temporarily be the same as current until its filled.
        every { restOperations.getForObject("http://localhost:8082/feed/3", String::class.java) } returns feedPage(
            self = "/3",
            current = "",
            prevArchive = "/2",
            entryRange = 9 downTo 9
        )
        every { restOperations.getForObject("http://localhost:8082/feed/2", String::class.java) } returns feedPage(
            self = "/2",
            current = "",
            prevArchive = "/1",
            nextArhive = "/3",
            entryRange = 8 downTo 6
        )
        every { restOperations.getForObject("http://localhost:8082/feed/1", String::class.java) } returns feedPage(
            self = "/1",
            current = "",
            prevArchive = "/0",
            nextArhive = "/2",
            entryRange = 5 downTo 3
        )
        every { restOperations.getForObject("http://localhost:8082/feed/0", String::class.java) } returns feedPage(
            self = "/0",
            current = "",
            nextArhive = "/1",
            entryRange = 2 downTo 1
        )
    }

    @Nested
    inner class StreamAllTest {
        @Test
        fun `it should stream events in order from beginning`() {

            val entryIds = feedConsumer.streamAll("http://localhost:8082/feed")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9)
        }

        @Test
        fun `it should produce an empty stream if feed is empty`() {
            clearMocks(restOperations)
            every { restOperations.getForObject("http://localhost:8082/feed", String::class.java) } returns feedPage()
            val entryIds = feedConsumer.streamAll("http://localhost:8082/feed")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).isEmpty()
        }

        @Test
        fun `it should produce a stream if current feed is empty but it has archives`() {
            clearMocks(restOperations)
            every { restOperations.getForObject("http://localhost:8082/feed", String::class.java) } returns feedPage(
                prevArchive = "/0"
            )
            every { restOperations.getForObject("http://localhost:8082/feed/0", String::class.java) } returns feedPage(
                self = "/0",
                current = "",
                entryRange = 2 downTo 1
            )
            val entryIds = feedConsumer.streamAll("http://localhost:8082/feed")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).containsExactly(1, 2)
        }
    }

    @Nested
    inner class EventsAfterTest {

        @Test
        fun `it should be able to resume feed processing from a specific page`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed/2", "urn:uuid:7")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())

            verify(exactly = 2) {
                restOperations.getForObject("http://localhost:8082/feed/2", String::class.java)
            }

            verify(exactly = 1) {
                restOperations.getForObject("http://localhost:8082/feed/3", String::class.java)
            }

            verify(exactly = 0) {
                restOperations.getForObject("http://localhost:8082/feed", String::class.java)
                restOperations.getForObject("http://localhost:8082/feed/1", String::class.java)
                restOperations.getForObject("http://localhost:8082/feed/0", String::class.java)
            }

            assertThat(entryIds).containsExactly(8, 9)
        }

        @Test
        fun `it should stream events in order after last processed`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed", "urn:uuid:2")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).containsExactly(3, 4, 5, 6, 7, 8, 9)
        }

        @Test
        fun `it should stream events in order after last processed - last event on archive page`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed", "urn:uuid:5")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).containsExactly(6, 7, 8, 9)
        }

        @Test
        fun `it should stream events in order after last processed - first event on archive page`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed", "urn:uuid:3")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).containsExactly(4, 5, 6, 7, 8, 9)
        }

        @Test
        fun `it should stream events in order after last processed - last event on latest archive`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed", "urn:uuid:8")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).containsExactly(9)
        }

        @Test
        fun `it should stream events in order after last processed - first event on latest archive`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed", "urn:uuid:6")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).containsExactly(7, 8, 9)
        }

        @Test
        fun `it should produce an empty stream if up to date`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed", "urn:uuid:9")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .collect(Collectors.toList())
            assertThat(entryIds).isEmpty()
        }

        @Test
        fun `it should throw an exception if last processed id is not found in the feed`() {
            assertThat {
                feedConsumer.eventsAfter("http://localhost:8082/feed", "urn:uuid:bogus-id")
                    .map { it.id.removePrefix("urn:uuid:").toInt() }
                    .collect(Collectors.toList())
            }.isFailure()
//            assertThat(exception).isNotNull {
//                it.message().isEqualTo("Last processed entryId urn:uuid:bogus-id not found in feed")
//            }
        }

        @Test
        fun `it should only read subsequent pages once events on the current page have all been consumed`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed/1", "urn:uuid:4")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .limit(1)
                .toList()

            // don't read any other pages, especially any subsequent ones
            verify(exactly = 0) {
                restOperations.getForObject("http://localhost:8082/feed", String::class.java)
                restOperations.getForObject("http://localhost:8082/feed/0", String::class.java)
                restOperations.getForObject("http://localhost:8082/feed/2", String::class.java)
                restOperations.getForObject("http://localhost:8082/feed/3", String::class.java)
            }

            assertThat(entryIds.count()).isEqualTo(1)
        }

        @Test
        fun `it should only read one subsequent pages when we consume all the events on the current page and ask for one more`() {
            val entryIds = feedConsumer.eventsAfter("http://localhost:8082/feed/1", "urn:uuid:4")
                .map { it.id.removePrefix("urn:uuid:").toInt() }
                .limit(3)
                .toList()

            // don't read any other pages, especially any subsequent ones
            verify(exactly = 0) {
                restOperations.getForObject("http://localhost:8082/feed", String::class.java)
                restOperations.getForObject("http://localhost:8082/feed/0", String::class.java)
                restOperations.getForObject("http://localhost:8082/feed/3", String::class.java)
            }

            assertThat(entryIds.count()).isEqualTo(3)
        }
    }

    fun feedPage(
        baseUrl: String = "http://localhost:8082/feed",
        self: String = "",
        via: String? = null,
        current: String? = null,
        prevArchive: String? = null,
        nextArhive: String? = null,
        entryRange: IntProgression? = null
    ) = """
        <?xml version="1.0" encoding="UTF-8"?>
        <feed xmlns="http://www.w3.org/2005/Atom">
          <title>Waiter Service Event Feed</title>
          <link rel="self" href="${baseUrl}$self" />
          ${current?.let { "<link rel=\"current\" href=\"$baseUrl$current\" />" } ?: ""}
          ${via?.let { "<link rel=\"via\" href=\"$baseUrl$via\" />" } ?: ""}
          ${prevArchive?.let { "<link rel=\"prev-archive\" href=\"$baseUrl$prevArchive\" />" } ?: ""}
          ${nextArhive?.let { "<link rel=\"next-archive\" href=\"$baseUrl$nextArhive\" />" } ?: ""}
          <author>
            <name>Monkey Feed</name>
          </author>
          <id>urn:uuid:fe83ac63-0424-47f6-98d0-bbb541aeea40</id>
          <updated>2018-12-11T04:51:14Z</updated>
          ${entryRange?.map { feedEntry(it) }?.joinToString("\n") ?: ""}
        </feed>
    """.trimIndent()

    fun feedEntry(sequence: Int) = """
            <entry>
                <title>OrderTakenEvent sequence: $sequence entryId: urn:uuid:$sequence entityId: urn:uuid:d1d5262b-75b3-9e5a-c506-8ec08571ac36</title>
                <id>urn:uuid:$sequence</id>
                <updated>2018-12-11T04:51:14Z</updated>
                <content type="application/vnd.cafe.order-taken-event+json">some content</content>
                <summary type="text">OrderTakenEvent occurred on entity $sequence</summary>
              </entry>"""
}
