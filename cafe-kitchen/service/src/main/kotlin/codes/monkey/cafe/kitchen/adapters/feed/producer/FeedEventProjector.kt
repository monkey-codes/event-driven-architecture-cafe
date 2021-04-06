package codes.monkey.cafe.kitchen.adapters.feed.producer

import codes.monkey.cafe.kitchen.domain.model.OrderPreparationCompletedEvent
import codes.monkey.cafe.kitchen.domain.model.OrderPreparationStartedEvent
import codes.monkey.cafe.kitchen.events.Item
import codes.monkey.cafe.starter.feed.producer.FeedEntry
import codes.monkey.cafe.starter.feed.producer.FeedEntryRepository
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.springframework.stereotype.Service

@Service
class FeedEventProjector(val feedEntryRepository: FeedEntryRepository) {

    @EventHandler(payloadType = OrderPreparationCompletedEvent::class)
    fun onOrderPreparationCompletedEvent(event: EventMessage<OrderPreparationCompletedEvent>) {
        event.payload.apply {
            feedEntryRepository.save(
                    FeedEntry.create(
                            entryId = event.identifier,
                            entityId = orderId.toString(),
                            created = event.timestamp,
                            version = 1,
                            payload = codes.monkey.cafe.kitchen.events.OrderPreparationCompletedEvent(
                                    waiterId = waiterId,
                                    orderId = orderId,
                                    cookId = cookId

                            )
                    )
            )
        }
    }

    @EventHandler(payloadType = OrderPreparationStartedEvent::class)
    fun onOrderPreparationStartedEvent(event: EventMessage<OrderPreparationStartedEvent>) {
        event.payload.apply {
            feedEntryRepository.save(
                    FeedEntry.create(
                            entryId = event.identifier,
                            entityId = orderId.toString(),
                            created = event.timestamp,
                            version = 1,
                            payload = codes.monkey.cafe.kitchen.events.OrderPreparationStartedEvent(
                                    waiterId = waiterId,
                                    orderId = orderId,
                                    cookId = cookId,
                                    items = items.map { Item(name =  it.name, quantity = it.quantity) }
                            )
                    )
            )
        }
    }
}