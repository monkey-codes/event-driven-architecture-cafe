package codes.monkey.cafe.cashier.adapters.feed.producer

import codes.monkey.cafe.cashier.domain.model.BillCreatedEvent
import codes.monkey.cafe.cashier.events.BillItem
import codes.monkey.cafe.starter.feed.producer.FeedEntry
import codes.monkey.cafe.starter.feed.producer.FeedEntryRepository
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.springframework.stereotype.Service

@Service
class FeedEventProjector(val feedEntryRepository: FeedEntryRepository) {

    @EventHandler(payloadType = BillCreatedEvent::class)
    fun onOrderPreparationCompletedEvent(event: EventMessage<BillCreatedEvent>) {
        event.payload.apply {
            feedEntryRepository.save(
                    FeedEntry.create(
                            entryId = event.identifier,
                            entityId = id.toString(),
                            created = event.timestamp,
                            version = 1,
                            payload = codes.monkey.cafe.cashier.events.BillCreatedEvent(
                                    id = id,
                                    orderId = orderId,
                                    items = items.map { BillItem(
                                            name = it.name,
                                            quantity = it.quantity,
                                            total = it.total
                                    ) }
                            )
                    )
            )
        }
    }

}