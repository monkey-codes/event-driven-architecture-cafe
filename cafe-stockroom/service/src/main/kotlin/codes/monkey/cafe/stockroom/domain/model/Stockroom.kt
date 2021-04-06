package codes.monkey.cafe.stockroom.domain.model

import codes.monkey.cafe.starter.logging.Logging
import codes.monkey.cafe.starter.logging.logEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.Logger
import java.lang.IllegalStateException
import java.util.*

@Aggregate
class Stockroom() {

    companion object {
        val LOGGER: Logger = Logging.loggerFor<Stockroom>()
    }

    private lateinit var stock: MutableMap<String, Int>

    @AggregateIdentifier
    var id: UUID? = null

    @CommandHandler
    constructor(createStockroomCommand: CreateStockroomCommand) : this() {
        apply(
                StockroomCreatedEvent(
                        id = createStockroomCommand.id,
                        initialStock = createStockroomCommand.initialStock
                )
        )
    }

    @CommandHandler
    fun handle(command: UseStockCommand) {
        if (!command.stock.all { stock.containsKey(it.name) && stock[it.name]!! >= it.quantity }) {
            throw IllegalStateException("Cannot use stock you don't have")
        }
        apply(StockUsedEvent(
                id = command.id,
                stock = command.stock
        ))
    }

    @CommandHandler
    fun handle(command: ReStockStockroomCommand) {
        apply(StockroomReStockedEvent(
                id = command.id,
                newStock = command.newStock
        ))
    }

    @EventSourcingHandler
    fun on(event: StockroomCreatedEvent) {
        LOGGER.logEvent(event)
        this.id = event.id
        this.stock = event.initialStock.map { it.name to it.quantity }.toMap().toMutableMap()
    }

    @EventSourcingHandler
    fun on(event: StockUsedEvent) {
        LOGGER.logEvent(event)
        event.stock.forEach {
            this.stock.compute(it.name) { _, currentValue -> currentValue!! - it.quantity }
        }
        this.stock
                .filter { it.value == 0 }
                .map { it.key }
                .toList()
                .takeIf { it.isNotEmpty() }
                ?.let {
                    apply(
                            OutOfStockEvent(id = this.id!!, stock = it)
                    )
                }
    }

    @EventSourcingHandler
    fun on(event: StockroomReStockedEvent) {
        LOGGER.logEvent(event)
        event.newStock.forEach { newStockItem ->
            this.stock.compute(newStockItem.name) {_, currentValue ->
                currentValue
                        ?.let { it -> it + newStockItem.quantity } ?: newStockItem.quantity
            }
        }
    }
}