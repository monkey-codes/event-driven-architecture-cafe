package codes.monkey.cafe.starter.feed.producer

import com.rometools.rome.feed.atom.Category
import com.rometools.rome.feed.atom.Content
import com.rometools.rome.feed.atom.Entry
import com.rometools.rome.feed.atom.Feed
import com.rometools.rome.feed.atom.Link
import com.rometools.rome.feed.atom.Person
import java.time.Instant
import java.time.OffsetDateTime
import java.util.Base64
import java.util.Date
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class FeedBuilder(@Autowired val feedConfig: FeedConfig) {
    fun build(baseHref: String, page: Page<FeedEntry>): Feed {
        return Feed().apply {
            feedType = "atom_1.0"
            id = baseHref
            title = feedConfig.title
            otherLinks = buildOtherLinks(baseHref, page)
            authors = listOf(Person().apply { name = feedConfig.author })
            updated = if (!page.isEmpty) date(page.content.last().created) else Date.from(Instant.EPOCH)
            entries = page.content.reversed().map { buildEntry(it, baseHref) }
        }
    }

    private fun buildOtherLinks(baseHref: String, page: Page<FeedEntry>): List<Link> {
        val isSubscription = page.isLast
        val hasNextArchive = page.number < page.totalPages - 1
        val hasPrevArchive = !page.isFirst
        return listOfNotNull(
                if (isSubscription) Link().apply { rel = "self"; href = baseHref } else null,
                if (isSubscription) Link().apply { rel = "via"; href = "$baseHref/${page.number}" } else null,
                if (!isSubscription) Link().apply { rel = "current"; href = baseHref } else null,
                if (!isSubscription) Link().apply { rel = "self"; href = "$baseHref/${page.number}" } else null,
                if (hasNextArchive) Link().apply { rel = "next-archive"; href = "$baseHref/${page.number + 1}" } else null,
                if (hasPrevArchive) Link().apply { rel = "prev-archive"; href = "$baseHref/${page.number - 1}" } else null
        )
    }

    private fun buildEntry(event: FeedEntry, baseHref: String): Entry {
        return Entry().apply {
            title = "${event.type} sequence: ${event.sequenceNumber} entryId: urn:uuid:${event.entryId} entityId: urn:uuid:${event.entityId}"
            id = "urn:uuid:${event.entryId}"
            alternateLinks = listOf(Link().apply { rel = "self"; href = "$baseHref-entry/${event.entryId}" })
            updated = date(event.created)
            contents = listOf(Content().apply { type = mimeType; value = base64(event.payload) })
            summary = Content().apply { type = "text/plain"; value = "${event.type} occurred on entity ${event.entityId}" }
            categories = listOf(Category().apply {
                scheme = "http://cafe/events/categories/type"
                term = event.type
            })
        }
    }

    private fun date(date: OffsetDateTime) = Date.from(date.toInstant())
    private fun base64(s: String) = Base64.getEncoder().encodeToString(s.toByteArray(Charsets.UTF_8))

    companion object {
        private const val mimeType = "application/vnd.cafe.event-envelope+json"
    }
}

@Component
@ConfigurationProperties(prefix = "atomfeed.server")
class FeedConfig() {

    constructor(pageSize: Int, title: String, author: String) : this() {
        this.pageSize = pageSize
        this.title = title
        this.author = author
    }

    var pageSize: Int = -1
    lateinit var title: String
    lateinit var author: String
}
