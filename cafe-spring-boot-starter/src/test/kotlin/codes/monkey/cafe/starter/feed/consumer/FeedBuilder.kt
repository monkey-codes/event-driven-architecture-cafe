package codes.monkey.cafe.starter.feed.consumer

import com.rometools.rome.feed.atom.Category
import com.rometools.rome.feed.atom.Content
import com.rometools.rome.feed.atom.Entry
import com.rometools.rome.feed.atom.Feed
import com.rometools.rome.feed.atom.Link
import java.util.Base64
import java.util.stream.Stream
import kotlin.streams.toList

class FeedBuilder {
    fun build(): Feed {
        val feed = Feed()

        val events = Stream.of(
            Entry().apply {
                id = "1"
                categories = listOf(Category().apply {
                    term = "WaiterCreatedEvent"
                })
                alternateLinks = listOf(
                    Link().apply {
                        rel = "self"
                        href = "http://producer/feed-entry/1"
                    }
                )
                contents.add(Content().apply {
                    value = base64(testEvent(
                        "application/vnd.cafe.waiter-created-event-v1+json",
                        "application/vnd.cafe.waiter-created-event-v2+json"
                    ))
                })
                source = feed
            },
            Entry().apply {
                id = "2"
                categories = listOf(Category().apply {
                    term = "IgnoredEvent"
                })
                alternateLinks = listOf(
                    Link().apply {
                        rel = "self"
                        href = "http://producer/feed-entry/2"
                    }
                )
                contents.add(Content().apply {
                    value = base64(testEvent(
                        "application/vnd.cafe.order-ignored-event-v1+json",
                        "application/vnd.cafe.order-ignored-event-v2+json"
                    ))
                })
                source = feed
            },
            Entry().apply {
                id = "3"
                categories = listOf(Category().apply {
                    term = "OrderTakenEvent"
                })
                alternateLinks = listOf(
                    Link().apply {
                        rel = "self"
                        href = "http://producer/feed-entry/3"
                    }
                )
                contents.add(Content().apply {
                    value = base64(testEvent(
                        "application/vnd.cafe.order-taken-event-v1+json",
                        "application/vnd.cafe.order-taken-event-v2+json"
                    ))
                })
                source = feed
            }
        )

        feed.apply {
            entries = events.toList()
            alternateLinks = listOf(
                Link().apply {
                    rel = "self"
                    href = "http://producer/feed"
                },
                Link().apply {
                    rel = "via"
                    href = "http://producer/feed/1"
                }
            )
        }

        return feed
    }

    private fun base64(s: String) = Base64.getEncoder().encodeToString(s.toByteArray(Charsets.UTF_8))

    private fun testEvent(vararg contentTypes: String): String {

        return """{
      "payloads": [
        ${contentTypes.map {
            """{
                 "contentType": "$it",
                 "content": {
                    "property": "$it"
                 }
                }
            """.trimIndent()
        }.joinToString(",\n")}
      ]
    }"""
    }
}
