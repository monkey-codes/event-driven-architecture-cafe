package codes.monkey.cafe.kitchen.domain.model

import codes.monkey.cafe.starter.logging.Logging
import codes.monkey.cafe.starter.logging.logEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.deadline.DeadlineManager
import org.axonframework.deadline.annotation.DeadlineHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.*
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.Logger
import java.time.Duration
import java.util.*

@Aggregate
class Cook {

    companion object {
        val LOGGER: Logger = Logging.loggerFor<Cook>()
    }

    @AggregateIdentifier
    var id: UUID? = null

    @AggregateMember
    var orderInProgress: Order? = null

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(command: PrepareOrderCommand, deadlineManager: DeadlineManager) {
        if (orderInProgress != null) throw AlreadyBusyWithAnotherOrderException(orderId = orderInProgress?.orderId)
        AggregateLifecycle.apply(OrderPreparationStartedEvent(
                cookId = command.cookId,
                orderId = command.orderId,
                waiterId = command.waiterId,
                items = command.items
        ))
        deadlineManager.schedule(Duration.ofSeconds(5), "orderPreparationTimer")
    }

    @EventSourcingHandler
    fun on(event: OrderPreparationStartedEvent) {
        LOGGER.logEvent(event)
        this.id = event.cookId
        this.orderInProgress = Order(orderId = event.orderId,
                items = event.items,
                waiterId = event.waiterId)
    }

    @EventSourcingHandler
    fun on(event: OrderPreparationCompletedEvent) {
        LOGGER.logEvent(event)
        this.orderInProgress = null
    }

    @DeadlineHandler(deadlineName = "orderPreparationTimer")
    fun onOrderPreparationTimeComplete() {
        AggregateLifecycle.apply(OrderPreparationCompletedEvent(
                cookId = this.id!!,
                orderId = this.orderInProgress!!.orderId,
                waiterId = this.orderInProgress!!.waiterId
        ))
    }


}
