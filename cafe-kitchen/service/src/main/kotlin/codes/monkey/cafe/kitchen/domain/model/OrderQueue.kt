package codes.monkey.cafe.kitchen.domain.model

import codes.monkey.cafe.starter.logging.Logging
import codes.monkey.cafe.starter.logging.logEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.*
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.Logger
import java.util.*

@Aggregate
class OrderQueue {

    companion object {
        val LOGGER: Logger = Logging.loggerFor<OrderQueue>()
    }

    @AggregateIdentifier
    var id: UUID? = null

    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    val orders = mutableListOf<Order>()

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(command: QueueOrderCommand) {
        AggregateLifecycle.apply(OrderQueuedEvent(
                queueId = command.queueId,
                orderId = command.orderId,
                waiterId = command.waiterId,
                items = command.items
        ))
    }

    @CommandHandler
    fun handle(command: DequeueOrderCommand) {
        AggregateLifecycle.apply(OrderDequeuedEvent(
                queueId = command.queueId,
                orderId = command.orderId
        ))
    }

    @EventSourcingHandler
    fun on(event: OrderQueuedEvent) {
        LOGGER.logEvent(event)
        this.id = event.queueId
        this.orders.add(Order(orderId = event.orderId,
                items = event.items,
                waiterId = event.waiterId))
    }

    @EventSourcingHandler
    fun on(event: OrderDequeuedEvent) {
        LOGGER.logEvent(event)
        this.orders.removeAll { it.orderId == event.orderId }
    }

}