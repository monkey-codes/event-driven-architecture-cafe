package codes.monkey.cafe.cashier.query.activity

import codes.monkey.cafe.cashier.domain.model.BillCreatedEvent
import codes.monkey.cafe.starter.activity.query.ActivityProjectionBase
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
class ActivityProjection(
        queryUpdateEmitter: QueryUpdateEmitter
): ActivityProjectionBase(queryUpdateEmitter) {

    @EventHandler(payloadType = BillCreatedEvent::class)
    fun onOrderQueued(event: EventMessage<BillCreatedEvent>) {
        emit(event, event.payload.id, "Bill created")
    }

}