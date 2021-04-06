package codes.monkey.cafe.starter.feed.producer

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import codes.monkey.cafe.starter.feed.objectMapper
import com.jayway.jsonpath.JsonPath
import java.time.Instant
import java.time.OffsetDateTime
import org.junit.jupiter.api.Test

internal class FeedEntryTest {

    @Test
    fun `it should serialize PublicEvent to json with type information`() {
        val submitted = Instant.now()
        val entry = FeedEntry.create(
            entryId = "1",
            entityId = "1",
            payloads = listOf(
                Payload(
                    version = 1,
                    content = TestDataFactory.testEvent(data = "blah", submitted = submitted)
                )
            )
        )
        val jsonPath = parseJson(entry.payload)

        assertThat(entry.entityId).isEqualTo("1")
        assertThat(entry.entryId).isNotEmpty()
        assertThat(entry.type).isEqualTo("TestEvent")
        assertThat(entry.created).isBetween(OffsetDateTime.now().minusSeconds(10), OffsetDateTime.now())
        assertThat(jsonPath("$.payloads") as List<*>).hasSize(1)
        assertThat(jsonPath("$.payloads[0].content.data")).isEqualTo("blah")
        assertThat(jsonPath("$.payloads[0].content.submitted")).isEqualTo(submitted.toString())
        assertThat(jsonPath("$.payloads[0].contentType")).isEqualTo("application/vnd.cafe.test-event-v1+json")
    }

    @Test
    fun `it should serialize two versions of an event to json with type information`() {
        val entry = FeedEntry.create(
            entryId = "1",
            entityId = "1",
            payloads = listOf(
                Payload(
                    version = 2,
                    content = TestDataFactory.testEvent()
                ),
                Payload(
                    contentType = EventContentType.from(1, TestEvent::class.java),
                    content = TestDataFactory.testEventJSONNode()
                )
            )
        )

        val jsonPath = parseJson(entry.payload)

        assertThat(jsonPath("$.payloads") as List<*>).hasSize(2)
        assertThat(jsonPath("$.payloads[0].contentType")).isEqualTo("application/vnd.cafe.test-event-v1+json")
        assertThat(jsonPath("$.payloads[1].contentType")).isEqualTo("application/vnd.cafe.test-event-v2+json")
    }

    @Test
    fun `it should support explicitly setting the type`() {
        val entry = FeedEntry.create(
            entryId = "1",
            entityId = "1",
            type = TestDataFactory.testEvent().javaClass,
            payloads = listOf(
                Payload(
                    contentType = EventContentType.from(2, TestEvent::class.java),
                    content = objectMapper.valueToTree(TestDataFactory.testEventV2())
                ),
                Payload(
                    version = 3,
                    content = TestDataFactory.testEvent()
                )
            )
        )

        val jsonPath = parseJson(entry.payload)

        assertThat(jsonPath("$.payloads") as List<*>).hasSize(2)
        assertThat(jsonPath("$.payloads[0].contentType")).isEqualTo("application/vnd.cafe.test-event-v2+json")
        assertThat(jsonPath("$.payloads[1].contentType")).isEqualTo("application/vnd.cafe.test-event-v3+json")
    }

    private fun parseJson(json: String): (String) -> Any {
        val payload = JsonPath.parse(json)
        return { path -> payload.read<Any>(path) }
    }
}
