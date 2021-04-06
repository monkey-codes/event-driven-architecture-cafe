package codes.monkey.cafe.cashier.adapters.feed.consumer

import codes.monkey.cafe.cashier.domain.model.CreateBillCommand
import codes.monkey.cafe.cashier.domain.model.Item
import codes.monkey.cafe.starter.feed.consumer.EntryMapping
import codes.monkey.cafe.starter.feed.consumer.EventConsumerTemplate
import codes.monkey.cafe.starter.feed.consumer.EventVersion
import codes.monkey.cafe.waiter.events.OrderTakenEvent
import io.micrometer.core.instrument.MeterRegistry
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.util.*

@Component
class PublicFeedConsumer(
        val commandGateway: CommandGateway,
        restTemplate: RestOperations,
        meterRegistry: MeterRegistry

) : EventConsumerTemplate(
        restTemplate,
        meterRegistry,
        EventVersion("OrderTakenEvent", 1, OrderTakenEvent::class.java)
) {
    override fun consume(entryMapping: EntryMapping) {
        val (_, event) = entryMapping
        when (event) {
            is OrderTakenEvent -> commandGateway.sendAndWait<Any>(
                    CreateBillCommand(id = UUID.randomUUID(),
                            orderId = event.orderId,
                            items = event.items.map { Item(name = it.name, quantity = it.quantity) })
            )
        }
    }

}