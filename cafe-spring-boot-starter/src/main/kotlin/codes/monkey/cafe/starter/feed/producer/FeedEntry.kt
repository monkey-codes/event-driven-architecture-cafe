package codes.monkey.cafe.starter.feed.producer

import codes.monkey.cafe.starter.feed.PublicEvent
import codes.monkey.cafe.starter.feed.objectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "feed_entry")
data class FeedEntry private constructor(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val sequenceNumber: Long = 0,
    val entryId: String,
    val entityId: String,
    val type: String,
    val created: OffsetDateTime,
    val payload: String
) {
    companion object {

        @Suppress("LongParameterList") // Later: requires changing all services
        fun create(
                entryId: String,
                sequenceNumber: Long = 0,
                entityId: String,
                created: Instant,
                type: Class<out PublicEvent>? = null,
                version: Int,
                payload: PublicEvent
        ) =
            create(
                entryId = entryId,
                sequenceNumber = sequenceNumber,
                entityId = entityId,
                type = type,
                created = created.atOffset(ZoneOffset.UTC),
                payloads = listOf(
                    Payload(
                        version = version,
                        content = payload
                    )
                )
            )

        @Suppress("LongParameterList") // Later: requires changing all services
        fun create(
                entryId: String,
                sequenceNumber: Long = 0,
                entityId: String,
                created: Instant,
                type: Class<out PublicEvent>? = null,
                payload: List<Payload>
        ) =
            create(
                entryId = entryId,
                sequenceNumber = sequenceNumber,
                entityId = entityId,
                type = type,
                created = created.atOffset(ZoneOffset.UTC),
                payloads = payload
            )

        @Suppress("LongParameterList") // Later: requires changing all services
        fun create(
                entryId: String,
                sequenceNumber: Long = 0,
                entityId: String,
                created: OffsetDateTime = OffsetDateTime.now(),
                type: Class<out PublicEvent>? = null,
                payloads: List<Payload>
        ): FeedEntry {

            val payloadsSortedByVersion = payloads.sortedBy { it.contentType.version }
            val entryType = type?.simpleName ?: resolveType(payloadsSortedByVersion)
            return FeedEntry(
                sequenceNumber = sequenceNumber,
                entryId = entryId,
                entityId = entityId,
                type = entryType,
                created = created,
                payload = objectMapper.writeValueAsString(Envelope(payloads = payloadsSortedByVersion))
            )
        }

        private fun resolveType(payloads: List<Payload>) =
            payloads.last().contentType.type
    }
}

data class Payload(val contentType: EventContentType, val content: ObjectNode) {
    constructor(version: Int, content: PublicEvent) : this(EventContentType.from(version, content), objectMapper.valueToTree(content))
}

data class Envelope(val payloads: List<Payload>)
