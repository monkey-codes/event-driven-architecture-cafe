package codes.monkey.cafe.stockroom.adapters.feed.producer

import codes.monkey.cafe.starter.feed.producer.FeedEntry
import codes.monkey.cafe.starter.feed.producer.FeedEntryRepository
import codes.monkey.cafe.stockroom.domain.model.OutOfStockEvent
import codes.monkey.cafe.stockroom.domain.model.StockroomReStockedEvent
import codes.monkey.cafe.stockroom.events.Item
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.springframework.stereotype.Service

@Service
class FeedEventProjector(val feedEntryRepository: FeedEntryRepository) {

    @EventHandler(payloadType = OutOfStockEvent::class)
    fun onOutOfStockEvent(event: EventMessage<OutOfStockEvent>) {
        event.payload.apply {
            feedEntryRepository.save(
                    FeedEntry.create(
                            entryId = event.identifier,
                            entityId = id.toString(),
                            created = event.timestamp,
                            version = 1,
                            payload = codes.monkey.cafe.stockroom.events.OutOfStockEvent(
                                    id = id,
                                    stock = stock
                            )
                    )
            )
        }
    }

    @EventHandler(payloadType = StockroomReStockedEvent::class)
    fun onStockroomReStockedEvent(event: EventMessage<StockroomReStockedEvent>) {
        event.payload.apply {
            feedEntryRepository.save(
                    FeedEntry.create(
                            entryId = event.identifier,
                            entityId = id.toString(),
                            created = event.timestamp,
                            version = 1,
                            payload = codes.monkey.cafe.stockroom.events.StockroomReStockedEvent(
                                    id = id,
                                    newStock = newStock.map { Item(name = it.name, quantity = it.quantity) }
                            )
                    )
            )
        }
    }

}