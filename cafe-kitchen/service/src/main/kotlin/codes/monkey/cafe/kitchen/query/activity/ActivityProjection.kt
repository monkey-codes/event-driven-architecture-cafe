package codes.monkey.cafe.kitchen.query.activity

import codes.monkey.cafe.kitchen.domain.model.OrderPreparationCompletedEvent
import codes.monkey.cafe.kitchen.domain.model.OrderPreparationStartedEvent
import codes.monkey.cafe.kitchen.domain.model.OrderQueuedEvent
import codes.monkey.cafe.starter.activity.query.ActivityProjectionBase
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
class ActivityProjection(
        queryUpdateEmitter: QueryUpdateEmitter
): ActivityProjectionBase(queryUpdateEmitter) {

    @EventHandler(payloadType = OrderQueuedEvent::class)
    fun onOrderQueued(event: EventMessage<OrderQueuedEvent>) {
        emit(event, event.payload.waiterId, "Order queued")
    }

    @EventHandler(payloadType = OrderPreparationStartedEvent::class)
    fun onOrderPreparationStarted(event: EventMessage<OrderPreparationStartedEvent>) {
        emit(event, event.payload.cookId, "Cooking order")
    }

    @EventHandler(payloadType = OrderPreparationCompletedEvent::class)
    fun onOrderPreparationFinished(event: EventMessage<OrderPreparationCompletedEvent>) {
        emit(event, event.payload.cookId, "Order ready")
    }
}