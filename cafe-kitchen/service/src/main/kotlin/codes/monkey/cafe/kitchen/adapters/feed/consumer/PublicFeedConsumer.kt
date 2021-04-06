package codes.monkey.cafe.kitchen.adapters.feed.consumer

import codes.monkey.cafe.kitchen.domain.model.KitchenConstants
import codes.monkey.cafe.kitchen.domain.model.QueueOrderCommand
import codes.monkey.cafe.kitchen.domain.model.toKitchenItem
import codes.monkey.cafe.starter.feed.consumer.EntryMapping
import codes.monkey.cafe.starter.feed.consumer.EventConsumerTemplate
import codes.monkey.cafe.starter.feed.consumer.EventVersion
import codes.monkey.cafe.waiter.events.OrderTakenEvent
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
        EventVersion("OrderTakenEvent", 1, OrderTakenEvent::class.java)
) {
    override fun consume(entryMapping: EntryMapping) {
        val (_, event) = entryMapping
        when (event) {
            is OrderTakenEvent -> commandGateway.sendAndWait<Any>(QueueOrderCommand(queueId = KitchenConstants.ORDER_QUEUE_ID,
                    orderId = event.orderId,
                    waiterId = event.waiterId,
                    items = event.items.map { it.toKitchenItem() }))
        }
    }

}