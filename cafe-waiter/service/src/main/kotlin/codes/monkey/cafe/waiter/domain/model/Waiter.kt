package codes.monkey.cafe.waiter.domain.model

import codes.monkey.cafe.starter.logging.Logging
import codes.monkey.cafe.starter.logging.logEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.ForwardMatchingInstances
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.Logger
import java.util.*

@Aggregate
final class Waiter() {

    companion object {
        val LOGGER: Logger = Logging.loggerFor<Waiter>()
    }

    @AggregateIdentifier
    var id: UUID? = null

    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    val orders = mutableListOf<Order>()

    val outOfStockItems = mutableSetOf<String>()

    private lateinit var name: String

    @CommandHandler
    constructor(command: HireWaiterCommand) : this() {
        apply(WaiterHiredEvent(id = command.id, name = command.name))
    }

    @CommandHandler
    fun handle(command: TakeOrderCommand): UUID {
        if(command.items.any { this.outOfStockItems.contains(it.name) }){
            throw ItemsOutOfStockException(this.outOfStockItems.toList())
        }
        apply(OrderTakenEvent(command.waiterId, command.orderId, items = command.items))
        return command.orderId
    }

    @CommandHandler
    fun handle(command: DeliverOrderToTableCommand) {
        apply(OrderDeliveredEvent(command.waiterId, command.orderId))
    }

    @CommandHandler
    fun handle(command: StopTakingOrdersForOutOfStockItemsCommand) {
        apply(ItemsMarkedOutOfStockEvent(command.waiterId, command.items))
    }

    @CommandHandler
    fun handle(command: StartTakingOrdersForFreshlyStockedItemsCommand) {
        apply(ItemsMarkedInStockEvent(command.waiterId, command.items))
    }

    @EventSourcingHandler
    fun on(event: WaiterHiredEvent) {
        LOGGER.logEvent(event)
        this.id = event.id
        this.name = event.name
    }

    @EventSourcingHandler
    fun on(event: OrderTakenEvent) {
        LOGGER.logEvent(event)
        this.orders.add( Order(event.orderId, event.items))
    }

    @EventSourcingHandler
    fun on(event: OrderDeliveredEvent) {
        LOGGER.logEvent(event)
        this.orders.removeAll { it.orderId == event.orderId }
    }

    @EventSourcingHandler
    fun on(event: ItemsMarkedOutOfStockEvent) {
        LOGGER.logEvent(event)
        this.outOfStockItems.addAll(event.items)
    }

    @EventSourcingHandler
    fun on(event: ItemsMarkedInStockEvent) {
        LOGGER.logEvent(event)
        this.outOfStockItems.removeAll(event.items)
    }
}