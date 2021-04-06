package codes.monkey.cafe.cashier.domain.model

import codes.monkey.cafe.starter.logging.Logging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.Logger
import java.util.*

@Aggregate
class Bill() {


    companion object {
        val LOGGER: Logger = Logging.loggerFor<Bill>()
    }

    private lateinit var items: List<BillItem>

    @AggregateIdentifier
    var id: UUID? = null


    @CommandHandler
    constructor(command: CreateBillCommand) : this() {
        AggregateLifecycle.apply(BillCreatedEvent(id = command.id,
                orderId = command.orderId,
                items = command.items.map {
                    BillItem(name = it.name,
                            quantity = it.quantity,
                            total = Pricing.total(it)) })
        )
    }

    @EventSourcingHandler
    fun on(event: BillCreatedEvent){
        this.id = event.id
        this.items = event.items
    }
}