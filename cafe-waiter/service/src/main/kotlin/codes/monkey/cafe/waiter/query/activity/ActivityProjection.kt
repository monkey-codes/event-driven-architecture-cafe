package codes.monkey.cafe.waiter.query.activity

import codes.monkey.cafe.starter.activity.query.ActivityProjectionBase
import codes.monkey.cafe.waiter.domain.model.OrderDeliveredEvent
import codes.monkey.cafe.waiter.domain.model.OrderTakenEvent
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
class ActivityProjection(
        queryUpdateEmitter: QueryUpdateEmitter
): ActivityProjectionBase(queryUpdateEmitter) {

    @EventHandler(payloadType = OrderTakenEvent::class)
    fun onOrderTaken(event: EventMessage<OrderTakenEvent>) {
        emit(event, event.payload.waiterId, "Order taken")
    }

    @EventHandler(payloadType = OrderDeliveredEvent::class)
    fun onOrderDelivered(event: EventMessage<OrderDeliveredEvent>) {
        emit(event, event.payload.waiterId, "Order delivered")
    }

}
