package codes.monkey.cafe.starter.feed.consumer

import com.rometools.rome.feed.atom.Feed
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestOperations

@ExtendWith(MockKExtension::class)
internal class EventConsumerTemplateTest {

    @RelaxedMockK
    private lateinit var progressTracker: ProgressTracker

    @RelaxedMockK
    private lateinit var restOperations: RestOperations

    @RelaxedMockK
    private lateinit var meterRegistry: MeterRegistry
    private lateinit var feedPage: Feed

    private lateinit var eventConsumerTemplate: EventConsumerTemplate

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
        eventConsumerTemplate = EventConsumerTemplateImpl(restOperations, meterRegistry)

        feedPage = FeedBuilder().build()
    }

    @Test
    fun `it should record the most recently viewed feed page`() {
        eventConsumerTemplate.handle(feedPage.entries.stream(), progressTracker)

        verify { progressTracker.lastProcessed("1", "http://producer/feed/1") }
    }

    @Test
    fun `it should have recorded as many calls to the progress tracker as there are events on that feed page`() {
        eventConsumerTemplate.handle(feedPage.entries.stream(), progressTracker)

        verify(exactly = feedPage.entries.count()) { progressTracker.lastProcessed(any(), any()) }
    }

    internal class EventConsumerTemplateImpl(restTemplate: RestOperations, meterRegistry: MeterRegistry, vararg eventVersions: EventVersion) : EventConsumerTemplate(restTemplate,
        meterRegistry,
        *eventVersions) {
        override fun consume(entryMapping: EntryMapping) {
            // no-op
        }
    }
}
