package codes.monkey.cafe.starter.feed.producer

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EventContentTypeTest {

    @ParameterizedTest
    @ValueSource(ints = [1, 10])
    fun `it should construct a content type from publicEvent and version`(version: Int) {
        val contentType = EventContentType.from(version, TestDataFactory.testEvent())
        assertThat(contentType.vendorPrefix).isEqualTo("application/vnd.cafe")
        assertThat(contentType.type).isEqualTo("TestEvent")
        assertThat(contentType.version).isEqualTo(version)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 10])
    fun `it should parse content type string`(version: Int) {
        val contentType = EventContentType.parse("application/vnd.cafe.test-event-v$version+json")
        assertThat(contentType.vendorPrefix).isEqualTo("application/vnd.cafe")
        assertThat(contentType.type).isEqualTo("TestEvent")
        assertThat(contentType.version).isEqualTo(version)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 10])
    fun `toString should produce the contentType string`(version: Int) {
        val contentType = EventContentType.parse("application/vnd.cafe.test-event-v$version+json")
        assertThat(contentType.toString()).isEqualTo("application/vnd.cafe.test-event-v$version+json")
    }

    @Test
    fun `it should blow up when content type is unknown`() {
        val bogusContentType = "bogusvendor/vnd.test-event-v1+json"
        val exception = assertThat {
            EventContentType.parse(bogusContentType)
        }.isFailure().hasMessage("Unsupported contentType: $bogusContentType")
    }

    @Test
    fun `it should provide next and previous version`() {
        val contentType = EventContentType.from(2, TestDataFactory.testEvent())
        assertThat(contentType.nextVersion().version).isEqualTo(3)
        assertThat(contentType.previousVersion().version).isEqualTo(1)
    }

    @Test
    fun `it should construct a content type from string type and version`() {
        val contentType = EventContentType.from(1, "UncheckedType")
        assertThat(contentType.vendorPrefix).isEqualTo("application/vnd.cafe")
        assertThat(contentType.type).isEqualTo("UncheckedType")
        assertThat(contentType.version).isEqualTo(1)
    }
}
