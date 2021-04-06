package codes.monkey.cafe.stockroom.adapters.feed.consumer

import codes.monkey.cafe.kitchen.events.OrderPreparationStartedEvent
import codes.monkey.cafe.starter.feed.consumer.EntryMapping
import codes.monkey.cafe.starter.feed.consumer.EventConsumerTemplate
import codes.monkey.cafe.starter.feed.consumer.EventVersion
import codes.monkey.cafe.stockroom.domain.model.Item
import codes.monkey.cafe.stockroom.domain.model.StockroomConstants
import codes.monkey.cafe.stockroom.domain.model.UseStockCommand
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
        EventVersion("OrderPreparationStartedEvent", 1, OrderPreparationStartedEvent::class.java)
) {
    override fun consume(entryMapping: EntryMapping) {
        val (_, event) = entryMapping
        when (event) {
            is OrderPreparationStartedEvent -> commandGateway.sendAndWait<Any>(UseStockCommand(
                    id = StockroomConstants.STOCKROOM_ID,
                    stock = event.items.map { Item(name = it.name, quantity = it.quantity) }
            ))
        }
    }

}