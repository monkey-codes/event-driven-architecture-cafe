package codes.monkey.cafe.starter.feed.producer

import codes.monkey.cafe.starter.feed.PublicEvent
import codes.monkey.cafe.starter.feed.objectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant
import java.util.UUID

class TestDataFactory {

    companion object {

        fun testEvent(data: String = "data", submitted: Instant = Instant.now()) =
            TestEvent(data = data, submitted = submitted)

        fun testEventJSONNode(event: PublicEvent = testEvent()) =
            objectMapper.writeValueAsString(event).let {
                objectMapper.readTree(it)
            } as ObjectNode

        fun testEventV2(payload: String = "payload", submitted: Instant = Instant.now()) =
            TestEventV2(payload = payload, submitted = submitted)

        fun feedEntry(
            entryId: String = UUID.randomUUID().toString(),
            entityId: String = UUID.randomUUID().toString(),
            created: Instant = Instant.now(),
            version: Int = 1,
            payload: PublicEvent = testEvent()
        ) = FeedEntry.create(
            entryId = entryId,
            entityId = entityId,
            created = created,
            version = version,
            payload = payload

        )

        fun transitionFeedEntry(
            entryId: String = UUID.randomUUID().toString(),
            entityId: String = UUID.randomUUID().toString(),
            created: Instant = Instant.now(),
            payload: List<Payload> = listOf(
                Payload(1, testEvent()),
                Payload(2, testEvent())
            )
        ) = FeedEntry.create(
            entryId = entryId,
            entityId = entityId,
            created = created,
            payload = payload

        )
    }
}

data class TestEvent(val data: String, val submitted: Instant) : PublicEvent
data class TestEventV2(val payload: String, val submitted: Instant) : PublicEvent
