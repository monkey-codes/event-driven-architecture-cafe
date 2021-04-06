package codes.monkey.cafe.starter.feed.consumer

import codes.monkey.cafe.starter.feed.PublicEvent
import codes.monkey.cafe.starter.feed.consumer.ConsumptionStatus.ERROR
import codes.monkey.cafe.starter.feed.consumer.ConsumptionStatus.SUCCESS
import codes.monkey.cafe.starter.feed.objectMapper
import codes.monkey.cafe.starter.feed.producer.EventContentType
import com.fasterxml.jackson.databind.node.ArrayNode
import com.rometools.rome.feed.atom.Entry
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import java.util.Base64
import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations

class EventVersionSupport(vararg eventVersions: EventVersion) {

    companion object {
        val LOGGER = LoggerFactory.getLogger(EventVersionSupport::class.java)!!
    }

    private val lookup = eventVersions.map { it.type to it }.toMap()

    fun supportedEventTypes(): (entry: Entry) -> Boolean = {
        lookup.keys.contains(type(it))
    }

    fun toSupportedEventVersion(restOperations: RestOperations? = null): (entry: Entry) -> EntryMapping = { entry ->
        with(lookup[type(entry)]!!) {
            (objectMapper.readTree(base64Decode(entry.contents.first().value)).get("payloads") as ArrayNode)
                .filter { EventContentType.parse(it.get("contentType").asText()).version == version }
                .map { objectMapper.treeToValue(it.get("content"), targetType) }
                .map { EntryMapping(entry = entry, event = it) }
                .firstOrNull() ?: doContentNegotiation(restOperations, entry)
                ?.let {
                    if (!it.statusCode.is2xxSuccessful) {
                        throw ContentNegotiationFailedException("Atom entry content negotiation failed with HTTP status: ${it.statusCodeValue}")
                    }
                    EntryMapping(entry, it.body!!)
                }
            ?: throw NoSuchElementException("$type version $version not found in Atom entry content")
        }
    }

    @Suppress("TooGenericExceptionCaught") // Ignore: Generic handler to make sure feed issues are surfaced
    fun consume(
        meterRegistry: MeterRegistry,
        consumer: (EntryMapping) -> Unit
    ): (EntryMapping) -> Unit {
        return { entryMapping: EntryMapping ->
            val startTime = System.nanoTime()
            try {
                consumer(entryMapping)
                val entry = entryMapping.entry
                timerBuilder(entryMapping.event, SUCCESS)
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                LOGGER.info("Consumed ${entry.title}. Deserialized as ${entryMapping.event::class.java.simpleName}")
            } catch (e: Exception) {
                timerBuilder(entryMapping.event, ERROR)
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                throw PublicEventConsumptionFailed(entryMapping, e)
            }
        }
    }

    private fun timerBuilder(
            event: PublicEvent,
            status: ConsumptionStatus
    ) = Timer.builder("public.event.consumption")
        .tags(listOf(
            Tag.of("publicEventType", event::class.simpleName!!),
            Tag.of("status", status.name)
        ))
        .description("Timer of Consumption of Public Events")

    private fun EventVersion.doContentNegotiation(
        restOperations: RestOperations?,
        entry: Entry
    ): ResponseEntity<out PublicEvent>? {
        val selfLink = selfLink(entry)
        LOGGER.warn("Could not resolve version $version of $type in Atom content, falling back to content negotiation on $selfLink")
        return restOperations?.exchange(selfLink, GET, acceptHeader(), targetType)
    }

    private fun EventVersion.acceptHeader(): HttpEntity<Void> {
        return HttpEntity(
            HttpHeaders().apply {
                set("Accept", EventContentType.from(type = type, version = version).toString())
            }
        )
    }

    private fun selfLink(entry: Entry) = (entry.alternateLinks + entry.otherLinks).first { it.rel == "self" }.href

    private fun type(entry: Entry) = entry.categories.first().term

    private fun base64Decode(content: String) = Base64.getDecoder().decode(content).toString(Charsets.UTF_8)
}

data class EntryMapping(val entry: Entry, val event: PublicEvent)
data class EventVersion(val contentType: EventContentType, val targetType: Class<out PublicEvent>) {
    val type = contentType.type
    val version = contentType.version

    constructor(type: String, version: Int, targetType: Class<out PublicEvent>) : this(EventContentType.from(
        version = version,
        type = type), targetType)
}

enum class ConsumptionStatus {
    SUCCESS,
    ERROR
}

class ContentNegotiationFailedException(message: String) : Exception(message)
class PublicEventConsumptionFailed(entryMapping: EntryMapping, cause: Exception) : Exception(
    "Public event consumption is now BLOCKED! Failed to consume entryId:${entryMapping.entry.id} event:${entryMapping.event} cause: ${cause.message}", cause
)
