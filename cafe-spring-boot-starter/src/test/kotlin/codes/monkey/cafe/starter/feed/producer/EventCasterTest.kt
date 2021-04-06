package codes.monkey.cafe.starter.feed.producer

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import codes.monkey.cafe.starter.feed.PublicEvent
import codes.monkey.cafe.starter.feed.objectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EventCasterTest {

    lateinit var eventCaster: EventCaster

    @BeforeEach
    fun setup() {
        eventCaster = EventCaster(
            listOf(
                CastTestEventV1ToV2Caster(),
                CastTestEventV2ToV3Caster()
            )
        )
    }

    @Test
    fun `it should downcast multiple versions of an event`() {
        val resultPayload: Payload = eventCaster.cast(
            source = toVersionedEventNode(
                version = 3,
                event = CastTestEvent(attribute = "chuck norris")
            ),
            target = contentTypeForVersion(1)
        )
        val jsonPath = parseJson(resultPayload.content)
        assertThat(jsonPath("$.property")).isEqualTo("chuck norris")
        assertThat(resultPayload.contentType.toString()).isEqualTo("application/vnd.cafe.cast-test-event-v1+json")
    }

    @Test
    fun `it should not alter the input payload`() {
        val input = toVersionedEventNode(
            version = 3,
            event = CastTestEvent(attribute = "chuck norris")
        )
        val resultPayload: Payload = eventCaster.cast(
            source = input,
            target = contentTypeForVersion(1)
        )
        val jsonPath = parseJson(resultPayload.content)
        val inputJsonPath = parseJson(input.content)
        assertThat(jsonPath("$.property")).isEqualTo("chuck norris")
        assertThat(inputJsonPath("$.attribute")).isEqualTo("chuck norris")
        assertThat(resultPayload.contentType.toString()).isEqualTo("application/vnd.cafe.cast-test-event-v1+json")
    }

    @Test
    fun `it should downcast from middle version to oldest version`() {
        val resultNode: Payload = eventCaster.cast(
            source = toVersionedEventNode(
                version = 2,
                event = CastTestEventV2(field = "chuck norris")
            ),
            target = contentTypeForVersion(1)
        )
        val jsonPath = parseJson(resultNode.content)
        assertThat(jsonPath("$.property")).isEqualTo("chuck norris")
    }

    @Test
    fun `it should downcast from latest version to middle version`() {
        val resultNode: Payload = eventCaster.cast(
            source = toVersionedEventNode(
                version = 3,
                event = CastTestEvent(attribute = "chuck norris")
            ),
            target = contentTypeForVersion(2)
        )
        val jsonPath = parseJson(resultNode.content)
        assertThat(jsonPath("$.field")).isEqualTo("chuck norris")
    }

    @Test
    fun `it should upcast from oldest version to middle version`() {
        val resultNode: Payload = eventCaster.cast(
            source = toVersionedEventNode(
                version = 1,
                event = CastTestEventV1(property = "chuck norris")
            ),
            target = contentTypeForVersion(2)
        )
        val jsonPath = parseJson(resultNode.content)
        assertThat(jsonPath("$.field")).isEqualTo("chuck norris")
    }

    @Test
    fun `it return the node if its already at target version`() {
        val resultNode: Payload = eventCaster.cast(
            source = toVersionedEventNode(
                version = 3,
                event = CastTestEvent(attribute = "chuck norris")
            ),
            target = contentTypeForVersion(3)
        )
        val jsonPath = parseJson(resultNode.content)
        assertThat(jsonPath("$.attribute")).isEqualTo("chuck norris")
    }

    @Test
    fun `it should upcast multiple versions of an event`() {
        val resultNode: Payload = eventCaster.cast(
            source = toVersionedEventNode(
                version = 1,
                event = CastTestEventV1(property = "chuck norris")
            ),
            target = contentTypeForVersion(3)
        )
        val jsonPath = parseJson(resultNode.content)
        assertThat(jsonPath("$.attribute")).isEqualTo("chuck norris")
    }

    @Test
    fun `it should upcast from middle version of an event`() {
        val resultNode: Payload = eventCaster.cast(
            source = toVersionedEventNode(
                version = 2,
                event = CastTestEventV2(field = "chuck norris")
            ),
            target = contentTypeForVersion(3)
        )
        val jsonPath = parseJson(resultNode.content)
        assertThat(jsonPath("$.attribute")).isEqualTo("chuck norris")
    }

    @Test
    fun `it should blow up when trying to cast to an unknown version`() {
        assertThat {
            eventCaster.cast(
                source = toVersionedEventNode(
                    version = 3,
                    event = CastTestEvent(attribute = "chuck norris")
                ),
                target = contentTypeForVersion(4)
            )
        }.isFailure().hasMessage("Cannot cast CastTestEvent to version 4 because it is an unknown version")
    }

    @Test
    fun `it should blow up when trying to cast to an unknown event`() {
        assertThat {
            eventCaster.cast(
                source = toVersionedEventNode(
                    version = 3,
                    event = UnknownEvent(),
                    type = UnknownEvent::class.java
                ),
                target = contentTypeForVersion(1)
            )
        }.isFailure().hasMessage("No casters configured for UnknownEvent")
    }

    @Test
    fun `it should cast most recent event in a FeedEntry`() {
        val resultNode: Payload = eventCaster.cast(
            source = TestDataFactory.transitionFeedEntry(
                    payload = listOf(
                            Payload(2, CastTestEventV2(field = "chuck norris")),
                            Payload(3, CastTestEvent(attribute = "chuck norris"))
                    )
            ),
            target = contentTypeForVersion(1)
        )
        val jsonPath = parseJson(resultNode.content)
        assertThat(jsonPath("$.property")).isEqualTo("chuck norris")
    }

    @ParameterizedTest
    @ValueSource(strings = ["1,4", "2,1", "-2,-1", "0,0"])
    fun `it should only support single positive increment in a caster`(range: String) {
        val (lower, upper) = range.split(",").map { it.toInt() }
        val exception = assertThat {
            Between(lower, upper)
        }.isFailure().hasMessage("Caster range should be a single positive increment " +
                "example: ${Between(1, 2)} found Between(lower=$lower, upper=$upper)")
    }

    private fun toVersionedEventNode(
            version: Int,
            event: PublicEvent,
            type: Class<out PublicEvent> = CastTestEvent::class.java
    ) = Payload(
        contentType = EventContentType.from(version, type),
        content = objectMapper.valueToTree(event)
    )

    private fun contentTypeForVersion(version: Int) = EventContentType.from(version, CastTestEvent::class.java)

    private fun parseJson(json: ObjectNode): (String) -> Any {
        val payload = JsonPath.parse(objectMapper.writeValueAsString(json))
        return { path -> payload.read<Any>(path) }
    }
}

data class CastTestEventV1(val property: String = "value") : PublicEvent
data class CastTestEventV2(val field: String = "value") : PublicEvent
data class CastTestEvent(val attribute: String = "value") : PublicEvent
data class UnknownEvent(val attribute: String = "value") : PublicEvent

class CastTestEventV1ToV2Caster : SingleEventCasterSupport(type = "CastTestEvent", between = Between(1, 2)) {

    override fun castUp(node: ObjectNode): ObjectNode {
        // 1 -> 2
        with(node) {
            put("field", get("property").textValue())
            remove("property")
        }
        return node
    }

    override fun castDown(node: ObjectNode): ObjectNode {
        // 2 -> 1
        with(node) {
            put("property", get("field").textValue())
            remove("field")
        }
        return node
    }
}

class CastTestEventV2ToV3Caster : SingleEventCasterSupport(type = "CastTestEvent", between = Between(2, 3)) {

    override fun castUp(node: ObjectNode): ObjectNode {
        with(node) {
            put("attribute", get("field").textValue())
            remove("field")
        }
        return node
    }

    override fun castDown(node: ObjectNode): ObjectNode {
        with(node) {
            put("field", get("attribute").textValue())
            remove("attribute")
        }
        return node
    }
}
