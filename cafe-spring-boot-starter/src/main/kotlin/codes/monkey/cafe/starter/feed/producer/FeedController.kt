package codes.monkey.cafe.starter.feed.producer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.rometools.rome.io.WireFeedOutput
import java.io.OutputStreamWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.http.MediaType.APPLICATION_ATOM_XML_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import org.springframework.web.util.UriComponentsBuilder

@Controller
class FeedController(
        @Value("\${atomfeed.server.page-size}") private val pageSize: Int = 20,
        @Autowired private val feedEntryRepository: FeedEntryRepository,
        @Autowired private val feedBuilder: FeedBuilder,
        @Autowired private val eventCaster: EventCaster
) {

    companion object {
        fun currentPage(maxSequenceNumber: Long, pageSize: Int): Long =
            maxSequenceNumber / pageSize
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/feed", "/feed/{page}"], produces = [APPLICATION_ATOM_XML_VALUE])
    @ResponseBody
    fun getFeed(
        @PathVariable page: Int?,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestHeader("X-Forwarded-Proto", required = false, defaultValue = "http") protocol: String
    ): StreamingResponseBody {

        val maxSequenceNumber = feedEntryRepository.findMaxSequenceNumber()
        val impliedPage: Long = page?.toLong() ?: currentPage(maxSequenceNumber.orElse(0), pageSize)
        val rangeStart = impliedPage * pageSize
        val entries = feedEntryRepository.findBySequenceNumberBetween(
            rangeStart = rangeStart,
            rangeEnd = rangeStart + pageSize - 1, // End range is inclusive
            sort = Sort.by(ASC, "sequenceNumber")
        )
        val numberOfEntries = if (maxSequenceNumber.isPresent) maxSequenceNumber.get().plus(1) else 0L // We assume sequence number is 0-indexed
        val feed = feedBuilder.build(
            baseHref = uriComponentsBuilder.scheme(protocol).path("/feed").toUriString(),
            page = PageImpl(entries, PageRequest.of(impliedPage.toInt(), pageSize), numberOfEntries)
        )
        return StreamingResponseBody { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use {
                WireFeedOutput().output(feed, it)
            }
        }
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/feed-entry/{entryId}"])
    @ResponseBody
    fun getFeedEntry(
        @PathVariable entryId: String,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestHeader("X-Forwarded-Proto", required = false, defaultValue = "http") protocol: String,
        @RequestHeader("Accept", required = true) contentType: String
    ): ResponseEntity<ObjectNode>? {
        return feedEntryRepository.findByEntryId(entryId).map<ResponseEntity<ObjectNode>> {
                ResponseEntity.ok().body(eventCaster.cast(it, EventContentType.parse(contentType)).content)
        }.orElse(ResponseEntity.notFound().build())
    }
}
