package codes.monkey.cafe.starter.feed.producer

import codes.monkey.cafe.starter.feed.PublicEvent
import com.rometools.rome.feed.atom.Feed
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.http.MediaType.APPLICATION_ATOM_XML
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(MockKExtension::class)
@AutoConfigureMockMvc()
@WebMvcTest(controllers = [FeedController::class])
@TestPropertySource(properties = ["atomfeed.server.page-size = 10"])
internal class FeedControllerIT {

    @SpringBootApplication
    private class TestConfig {

        @Bean
        fun feedBuilder(): FeedBuilder = mockk(relaxed = true)

        @Bean
        fun feedEntryRepository(): FeedEntryRepository = mockk(relaxed = true)

        @Bean
        fun eventCaster(): EventCaster = mockk(relaxed = true)
    }

    @Autowired
    lateinit var mockMVC: MockMvc

    @Autowired
    lateinit var feedBuilder: FeedBuilder

    @Autowired
    lateinit var feedEntryRepository: FeedEntryRepository

    @Autowired
    lateinit var eventCaster: EventCaster

    private val feedEntryCount: Long = 100

    @BeforeEach
    fun setup() {
        every { feedEntryRepository.count() } returns feedEntryCount
        clearAllMocks()
    }

    @Test
    fun `it should load the current feed`() {
        val pageSize = 10
        val baseUrl = "http://localhost/feed"
        val currentPageNumber = 9
        val feedEntries = emptyList<FeedEntry>()
        val page = PageImpl<FeedEntry>(feedEntries, PageRequest.of(currentPageNumber, pageSize), feedEntryCount)
        val sort = Sort.by(ASC, "sequenceNumber")
        every { feedEntryRepository.findMaxSequenceNumber() } returns Optional.of(feedEntryCount - 1)
        every { feedEntryRepository.findBySequenceNumberBetween(any(), any(), any()) } returns feedEntries
        every { feedBuilder.build(baseUrl, page) } returns Feed()
        every { feedBuilder.build(any(), any()) } returns feed()
        asyncDispatch { httpGet("/feed") }.andExpect(status().isOk)
        verify { feedEntryRepository.findMaxSequenceNumber() }
        verify { feedEntryRepository.findBySequenceNumberBetween(90, 99, sort) }
        verify { feedBuilder.build(baseUrl, page) }
    }

    @Test
    fun `it should load the archived feeds`() {
        val pageSize = 10
        val baseUrl = "http://localhost/feed"
        val pageNumber = 0
        val feedEntries = emptyList<FeedEntry>()
        val page = PageImpl<FeedEntry>(feedEntries, PageRequest.of(pageNumber, pageSize), feedEntryCount)
        val sort = Sort.by(ASC, "sequenceNumber")
        every { feedEntryRepository.findMaxSequenceNumber() } returns Optional.of(feedEntryCount - 1)
        every { feedEntryRepository.findBySequenceNumberBetween(any(), any(), any()) } returns feedEntries
        every { feedBuilder.build(baseUrl, page) } returns Feed()
        every { feedBuilder.build(any(), any()) } returns feed()
        asyncDispatch { httpGet("/feed/0") }.andExpect(status().isOk)
        verify { feedEntryRepository.findBySequenceNumberBetween(0, 9, sort) }
        verify { feedBuilder.build(baseUrl, page) }
    }

    @Test
    fun `it should return empty page when no feed entries`() {
        val pageSize = 10
        val baseUrl = "http://localhost/feed"
        val pageNumber = 0
        val feedEntries = emptyList<FeedEntry>()
        val page = PageImpl<FeedEntry>(feedEntries, PageRequest.of(pageNumber, pageSize), 0)
        val sort = Sort.by(ASC, "sequenceNumber")
        every { feedEntryRepository.findMaxSequenceNumber() } returns Optional.empty()
        every { feedEntryRepository.findBySequenceNumberBetween(any(), any(), any()) } returns feedEntries
        every { feedBuilder.build(baseUrl, page) } returns Feed()
        every { feedBuilder.build(any(), any()) } returns feed()
        asyncDispatch { httpGet("/feed") }.andExpect(status().isOk)
        verify { feedEntryRepository.findBySequenceNumberBetween(0, 9, sort) }
        verify { feedBuilder.build(baseUrl, page) }
    }

    @Test
    fun `it should load a version of feed entry based on content negotiation`() {
        val entryId = UUID.randomUUID().toString()
        val testEvent = TestDataFactory.testEvent()
        val feedEntry = TestDataFactory.feedEntry(payload = testEvent)
        val requestedContent = EventContentType.from(1, testEvent)
        every { feedEntryRepository.findByEntryId(entryId) } returns Optional.of(feedEntry)
        every { eventCaster.cast(feedEntry, requestedContent) } returns versionedPayload(1, testEvent)

        mockMVC.perform(
            get("/feed-entry/$entryId")
                .accept(requestedContent.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data", Matchers.equalTo("data")))

        verify { feedEntryRepository.findByEntryId(entryId) }
        verify { eventCaster.cast(feedEntry, requestedContent) }
    }

    private fun versionedPayload(version: Int, event: PublicEvent) =
        Payload(version = version, content = event)

    @Test
    fun `it should return a 404 when an unknown version is requested`() {
        val entryId = UUID.randomUUID().toString()
        val testEvent = TestDataFactory.testEvent()
        val feedEntry = TestDataFactory.feedEntry(payload = testEvent)
        val requestedContent = EventContentType.from(1, testEvent)
        every { feedEntryRepository.findByEntryId(entryId) } returns Optional.of(feedEntry)
        every { eventCaster.cast(feedEntry, requestedContent) } throws VersionNotFoundException("event version not found")

        mockMVC.perform(
            get("/feed-entry/$entryId")
                .accept(requestedContent.toString())
        )
            .andExpect(status().isNotFound)

        verify { feedEntryRepository.findByEntryId(entryId) }
        verify { eventCaster.cast(feedEntry, requestedContent) }
    }

    @Test
    fun `it should return a 501 (NOT IMPLEMENTED) when no caster is configured for event type`() {
        val entryId = UUID.randomUUID().toString()
        val testEvent = TestDataFactory.testEvent()
        val feedEntry = TestDataFactory.feedEntry(payload = testEvent)
        val requestedContent = EventContentType.from(1, testEvent)
        every { feedEntryRepository.findByEntryId(entryId) } returns Optional.of(feedEntry)
        every { eventCaster.cast(feedEntry, requestedContent) } throws NoCasterConfiguredException("no caster configured")

        mockMVC.perform(
            get("/feed-entry/$entryId")
                .accept(requestedContent.toString())
        )
            .andExpect(status().`is`(501))

        verify { feedEntryRepository.findByEntryId(entryId) }
        verify { eventCaster.cast(feedEntry, requestedContent) }
    }

    @Test
    fun `it should return a 404 when an unknown event is requested`() {
        val entryId = UUID.randomUUID().toString()
        val testEvent = TestDataFactory.testEvent()
        val requestedContent = EventContentType.from(1, testEvent)
        every { feedEntryRepository.findByEntryId(entryId) } returns Optional.empty()

        mockMVC.perform(
            get("/feed-entry/$entryId")
                .accept(requestedContent.toString())
        )
            .andExpect(status().isNotFound)

        verify { feedEntryRepository.findByEntryId(entryId) }
    }

    private fun feed() =
            Feed().apply {
                feedType = "atom_1.0"
            }

    private fun httpGet(url: String, callback: MockHttpServletRequestBuilder.() -> Unit = {}) =
            mockMVC.perform(
                    get(url)
                            .accept(APPLICATION_ATOM_XML).apply(callback))
                    .andReturn()

    private fun asyncDispatch(callback: () -> MvcResult): ResultActions =
            mockMVC.perform(MockMvcRequestBuilders.asyncDispatch(callback()))
}
