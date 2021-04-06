package codes.monkey.cafe.stockroom.query.activity

import codes.monkey.cafe.starter.activity.query.ActivityProjectionBase
import codes.monkey.cafe.stockroom.domain.model.OutOfStockEvent
import codes.monkey.cafe.stockroom.domain.model.StockUsedEvent
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
class ActivityProjection(
        queryUpdateEmitter: QueryUpdateEmitter
): ActivityProjectionBase(queryUpdateEmitter) {

    @EventHandler(payloadType = StockUsedEvent::class)
    fun onStockUsed(event: EventMessage<StockUsedEvent>) {
        emit(event, event.payload.id, "Stock used")
    }

    @EventHandler(payloadType = OutOfStockEvent::class)
    fun onOutOfStock(event: EventMessage<OutOfStockEvent>) {
        emit(event, event.payload.id, "Out of stock")
    }
}
