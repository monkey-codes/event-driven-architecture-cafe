package codes.monkey.cafe.waiter.adapters.feed.consumer

import codes.monkey.cafe.kitchen.events.OrderPreparationCompletedEvent
import codes.monkey.cafe.starter.feed.consumer.EntryMapping
import codes.monkey.cafe.starter.feed.consumer.EventConsumerTemplate
import codes.monkey.cafe.starter.feed.consumer.EventVersion
import codes.monkey.cafe.stockroom.events.OutOfStockEvent
import codes.monkey.cafe.stockroom.events.StockroomReStockedEvent
import codes.monkey.cafe.waiter.domain.model.DeliverOrderToTableCommand
import codes.monkey.cafe.waiter.domain.model.StartTakingOrdersForFreshlyStockedItemsCommand
import codes.monkey.cafe.waiter.domain.model.StopTakingOrdersForOutOfStockItemsCommand
import codes.monkey.cafe.waiter.domain.model.WaiterConstants
import io.micrometer.core.instrument.MeterRegistry
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
class PublicFeedConsumer(
        val commandGateway: CommandGateway,
        restTemplate: RestOperations,
        meterRegistry: MeterRegistry

) : EventConsumerTemplate(
        restTemplate,
        meterRegistry,
        EventVersion("OrderPreparationCompletedEvent", 1, OrderPreparationCompletedEvent::class.java),
        EventVersion("OutOfStockEvent", 1, OutOfStockEvent::class.java),
        EventVersion("StockroomReStockedEvent", 1, StockroomReStockedEvent::class.java),
) {
    override fun consume(entryMapping: EntryMapping) {
        val (_, event) = entryMapping
        when (event) {
            is OrderPreparationCompletedEvent -> commandGateway.sendAndWait<Any>(DeliverOrderToTableCommand(
                    orderId = event.orderId,
                    waiterId = event.waiterId))
            is OutOfStockEvent -> commandGateway.sendAndWait<Any>(
                    StopTakingOrdersForOutOfStockItemsCommand(
                            waiterId = WaiterConstants.WAITER_ID,
                            items = event.stock))
            is StockroomReStockedEvent -> commandGateway.sendAndWait<Any>(
                    StartTakingOrdersForFreshlyStockedItemsCommand(
                            waiterId = WaiterConstants.WAITER_ID,
                            items = event.newStock.map { it.name }
                    )
            )
        }
    }

}