package codes.monkey.cafe.starter.feed.producer

import codes.monkey.cafe.starter.feed.PublicEvent
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class EventContentType private constructor(val vendorPrefix: String = "application/vnd.cafe", val type: String, val version: Int) {

    companion object {

        private val CONTENT_TYPE_REGEX = """(application\/vnd.cafe){1}\.(.*)-v(\d+)\+json""".toRegex()

        private val KEBAB_CASE_REGEX = Regex("""([a-z0-9])([A-Z])""")

        fun from(version: Int, type: String) =
            EventContentType(
                type = type,
                version = version
            )

        fun from(version: Int, type: Class<out PublicEvent>) =
            EventContentType(
                type = type.simpleName,
                version = version
            )

        fun from(version: Int, event: PublicEvent): EventContentType = from(version, event::class.java)

        @JsonCreator
        @JvmStatic
        fun parse(contentType: String): EventContentType =
            CONTENT_TYPE_REGEX
                .find(contentType)
                ?.let {
                    val (vendorPrefix, type, version) = it.destructured
                    EventContentType(
                        vendorPrefix = vendorPrefix,
                        type = type.split("-").map { it.capitalize() }.joinToString(""),
                        version = version.toInt()
                    )
                } ?: throw IllegalArgumentException("Unsupported contentType: $contentType")
    }

    @JsonValue
    override fun toString(): String {
        return "$vendorPrefix.${toKebabCase(type)}-v$version+json"
    }

    fun nextVersion() = this.copy(version = this.version + 1)
    fun previousVersion() = this.copy(version = this.version - 1)

    private fun toKebabCase(str: String) = str.replace(KEBAB_CASE_REGEX, "$1-$2").toLowerCase()
}
