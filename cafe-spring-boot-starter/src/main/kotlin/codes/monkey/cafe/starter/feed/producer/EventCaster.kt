package codes.monkey.cafe.starter.feed.producer

import codes.monkey.cafe.starter.feed.objectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus

@Service
class EventCaster(@Autowired(required = false) singleEventCasters: List<SingleEventCaster> = emptyList()) {

    private val castersByTypeDown: Map<String, List<SingleEventCaster>> = singleEventCasters
        .groupBy { it.type }
        .map { Pair(it.key, it.value.sortedByDescending { it.between.lower }) }
        .toMap()

    private val castersByTypeUp = castersByTypeDown
        .map { Pair(it.key, it.value.reversed()) }
        .toMap()

    fun cast(source: Payload, target: EventContentType): Payload {
        val contentType = source.contentType
        val targetVersion = target.version
        if (contentType.version == targetVersion) return source

        val (castersByType, direction) =
            if (targetVersion < contentType.version)
                Pair(castersByTypeDown, Direction.DOWN)
            else
                Pair(castersByTypeUp, Direction.UP)

        return castersByType[contentType.type]
            ?.filter(direction.filter(contentType, target))
            ?.fold(source.copy(content = source.content.deepCopy()), direction.cast)
            ?.also {
                if (it.contentType != target)
                    throw VersionNotFoundException("Cannot cast ${contentType.type} to version $targetVersion because it is an unknown version")
            }
            ?: throw NoCasterConfiguredException("No casters configured for ${contentType.type}")
    }

    fun cast(source: FeedEntry, target: EventContentType) =
        cast(
            source = objectMapper.readValue(source.payload, Envelope::class.java).payloads
                .sortedByDescending { it.contentType.version }
                .first(),
            target = target
        )
}

private enum class Direction(
        val cast: (acc: Payload, singleEventCaster: SingleEventCaster) -> Payload,
        val filter: (EventContentType, EventContentType) -> (SingleEventCaster) -> Boolean
) {
    UP(
        cast = { acc, singleEventCaster -> singleEventCaster.up(acc) },
        filter = { sourceContentType, targetContentType ->
            { caster: SingleEventCaster -> caster.lower() >= sourceContentType.version &&
                caster.upper() <= targetContentType.version } }
    ),
    DOWN(
        cast = { acc, singleEventCaster -> singleEventCaster.down(acc) },
        filter = { sourceContentType, targetContentType ->
            { caster: SingleEventCaster -> caster.upper() <= sourceContentType.version &&
                caster.lower() >= targetContentType.version } }
    )
}

data class Between(val lower: Int, val upper: Int) {
    init {
        if (upper - lower != 1 || lower < 0 || upper < 0) throw
            IllegalArgumentException("Caster range should be a single positive increment example: Between(lower=1, upper=2) found $this")
    }
}

interface SingleEventCaster {
    fun down(node: Payload): Payload
    fun up(node: Payload): Payload

    fun lower() = between.lower
    fun upper() = between.upper

    val type: String
    val between: Between
}

abstract class SingleEventCasterSupport(override val type: String, override val between: Between) : SingleEventCaster {

    override fun down(node: Payload): Payload {
        assertNodeMatchesVersion(node, between.upper)
        return Payload(contentType = node.contentType.previousVersion(), content = castDown(node.content))
    }

    override fun up(node: Payload): Payload {
        assertNodeMatchesVersion(node, between.lower)
        return Payload(contentType = node.contentType.nextVersion(), content = castUp(node.content))
    }

    protected abstract fun castDown(node: ObjectNode): ObjectNode
    protected abstract fun castUp(node: ObjectNode): ObjectNode

    private fun assertNodeMatchesVersion(node: Payload, version: Int) {
        if (node.contentType.version != version)
            throw IllegalArgumentException("Expected ${node.contentType.type} version $version but found ${node.contentType.version}")
    }
}

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Version Not Found")
class VersionNotFoundException(message: String) : IllegalArgumentException(message)

@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED, reason = "No casters configured for event")
class NoCasterConfiguredException(message: String) : IllegalArgumentException(message)
