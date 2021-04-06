package codes.monkey.cafe.waiter.adapters.feed.producer

import codes.monkey.cafe.starter.feed.producer.FeedEntry
import codes.monkey.cafe.starter.feed.producer.FeedEntryRepository
import codes.monkey.cafe.waiter.domain.model.OrderTakenEvent
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.springframework.stereotype.Service

@Service
class FeedEventProjector(val feedEntryRepository: FeedEntryRepository) {

    @EventHandler(payloadType = OrderTakenEvent::class)
    fun on(event: EventMessage<OrderTakenEvent>) {
        event.payload.apply {
            feedEntryRepository.save(
                    FeedEntry.create(
                            entryId = event.identifier,
                            entityId = orderId.toString(),
                            created = event.timestamp,
                            version = 1,
                            payload = codes.monkey.cafe.waiter.events.OrderTakenEvent(
                                    waiterId = waiterId,
                                    orderId = orderId,
                                    items = items
                            )
                    )
            )
        }
    }
}