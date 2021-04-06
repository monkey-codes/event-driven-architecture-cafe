package codes.monkey.cafe.starter.feed.consumer

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import codes.monkey.cafe.starter.feed.PublicEvent
import com.rometools.rome.feed.atom.Feed
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import java.util.stream.Collectors
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations

@ExtendWith(MockKExtension::class)
class EventVersionSupportTest {

    lateinit var feedPage: Feed

    @BeforeEach
    fun beforeEach() {
        feedPage = FeedBuilder().build()
    }

    @Test
    fun `it should filter events based on category and version`() {
        val filteredEvents = with(EventVersionSupport(
            EventVersion("WaiterCreatedEvent", 1, WaiterCreatedEventV1::class.java),
            EventVersion("OrderTakenEvent", 2, OrderTakenEventV2::class.java)
        )) {
            feedPage.entries
                .stream()
                .filter(supportedEventTypes())
                .map(toSupportedEventVersion())
                .map { it.event }
                .collect(Collectors.toList())
        }
        assertThat(filteredEvents).hasSize(2)
        assertThat(filteredEvents).containsAll(
            WaiterCreatedEventV1("application/vnd.cafe.waiter-created-event-v1+json"),
            OrderTakenEventV2("application/vnd.cafe.order-taken-event-v2+json")
        )
    }

    @Test
    fun `it should fallback to content negotiation if the version is not found and the restOperations is provided`() {
        val restOperations = mockk<RestOperations>(relaxed = true)
        val feedEntryUrl = "http://producer/feed-entry/3"

        every { restOperations.exchange(any<String>(), any(), any(), OrderTakenEventV3::class.java) } returns
            ResponseEntity.ok(OrderTakenEventV3(property = "application/vnd.cafe.order-taken-event-v3+json"))

        val filteredEvents = with(EventVersionSupport(
            EventVersion("WaiterCreatedEvent", 1, WaiterCreatedEventV1::class.java),
            EventVersion("OrderTakenEvent", 3, OrderTakenEventV3::class.java)
        )) {
            feedPage.entries
                .stream()
                .filter(supportedEventTypes())
                .map(toSupportedEventVersion(restOperations))
                .map { it.event }
                .collect(Collectors.toList())
        }
        verify {
            restOperations.exchange(
                feedEntryUrl,
                GET,
                match<HttpEntity<Void>> {
                    it.headers.accept.contains(MediaType("application", "vnd.cafe.order-taken-event-v3+json"))
                },
                OrderTakenEventV3::class.java
            )
        }
        assertThat(filteredEvents).hasSize(2)
        assertThat(filteredEvents).containsAll(
            WaiterCreatedEventV1("application/vnd.cafe.waiter-created-event-v1+json"),
            OrderTakenEventV3("application/vnd.cafe.order-taken-event-v3+json")
        )
    }

    @Test
    fun `it should throw a ContentNegotiationFailedException when api call fails`() {
        val restOperations = mockk<RestOperations>(relaxed = true)

        every { restOperations.exchange(any<String>(), any(), any(), OrderTakenEventV3::class.java) } returns
            ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()

        val exception = assertThrows<ContentNegotiationFailedException> {
            with(EventVersionSupport(
                EventVersion("WaiterCreatedEvent", 1, WaiterCreatedEventV1::class.java),
                EventVersion("OrderTakenEvent", 3, OrderTakenEventV3::class.java)
            )) {
                feedPage.entries
                    .stream()
                    .filter(supportedEventTypes())
                    .map(toSupportedEventVersion(restOperations))
                    .map { it.event }
                    .collect(Collectors.toList())
            }
        }
        assertThat(exception.message).isEqualTo("Atom entry content negotiation failed with HTTP status: 501")
    }

    @Test
    fun `it should throw a ElementNotFoundException when content negotiation is disabled by not providing restOperations`() {
        val exception = assertThrows<NoSuchElementException> {
            with(EventVersionSupport(
                EventVersion("WaiterCreatedEvent", 1, WaiterCreatedEventV1::class.java),
                EventVersion("OrderTakenEvent", 3, OrderTakenEventV3::class.java)
            )) {
                feedPage.entries
                    .stream()
                    .filter(supportedEventTypes())
                    .map(toSupportedEventVersion())
                    .map { it.event }
                    .collect(Collectors.toList())
            }
        }
        assertThat(exception.message).isEqualTo("OrderTakenEvent version 3 not found in Atom entry content")
    }

    @Test
    fun `it should time consumption in the consume template method`() {
        val progressTracker = mockk<ProgressTracker>(relaxed = true)
        val meterRegistry = SimpleMeterRegistry()
        val consumer: (EntryMapping) -> Unit = {}
        with(EventVersionSupport(
            EventVersion("WaiterCreatedEvent", 1, WaiterCreatedEventV1::class.java)
        )) {
            feedPage.entries
                .stream()
                .filter(supportedEventTypes())
                .map(toSupportedEventVersion())
                .forEach(consume(meterRegistry, consumer))
        }

        assertThat(meterRegistry["public.event.consumption"].tag("status", "SUCCESS").timer().count()).isEqualTo(1L)
    }

    @Test
    fun `it should handle errors in the consume template method`() {
        val progressTracker = mockk<ProgressTracker>(relaxed = true)
        val meterRegistry = SimpleMeterRegistry()
        val consumer: (EntryMapping) -> Unit = { throw IllegalStateException("Cannot consume event") }
        assertThrows<PublicEventConsumptionFailed> {
            with(EventVersionSupport(
                EventVersion("WaiterCreatedEvent", 1, WaiterCreatedEventV1::class.java)
            )) {
                feedPage.entries
                    .stream()
                    .filter(supportedEventTypes())
                    .map(toSupportedEventVersion())
                    .forEach(consume(meterRegistry, consumer))
            }
        }
        verify(exactly = 0) { progressTracker.lastProcessed("1", any()) }
        assertThat(meterRegistry["public.event.consumption"].tag("status", "ERROR").timer().count()).isEqualTo(1L)
    }
}

data class WaiterCreatedEventV1(val property: String) : PublicEvent
data class OrderTakenEventV2(val property: String) : PublicEvent
data class OrderTakenEventV3(val property: String) : PublicEvent
