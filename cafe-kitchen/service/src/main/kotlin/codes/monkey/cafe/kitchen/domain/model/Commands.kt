package codes.monkey.cafe.kitchen.domain.model

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class QueueOrderCommand(
        @TargetAggregateIdentifier val queueId: UUID = KitchenConstants.ORDER_QUEUE_ID,
        val orderId: UUID,
        val waiterId: UUID,
        val items: List<Item>)

data class PrepareOrderCommand(
        @TargetAggregateIdentifier val cookId: UUID,
        val orderId: UUID,
        val waiterId: UUID,
        val items: List<Item>)

data class DequeueOrderCommand(
        @TargetAggregateIdentifier val queueId: UUID = KitchenConstants.ORDER_QUEUE_ID,
        val orderId: UUID)