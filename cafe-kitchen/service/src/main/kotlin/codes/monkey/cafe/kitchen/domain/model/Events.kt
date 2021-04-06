package codes.monkey.cafe.kitchen.domain.model

import java.util.*

data class Item(val name: String, val quantity: Int)

fun codes.monkey.cafe.waiter.api.commands.Item.toKitchenItem() = Item(
        name = name,
        quantity = quantity
)

data class OrderQueuedEvent(
        val queueId: UUID,
        val orderId: UUID,
        val waiterId: UUID,
        val items: List<Item>
)

data class OrderPreparationStartedEvent(
        val cookId: UUID,
        val orderId: UUID,
        val waiterId: UUID,
        val items: List<Item>
)

data class OrderPreparationCompletedEvent(
        val cookId: UUID,
        val orderId: UUID,
        val waiterId: UUID,
)

data class OrderDequeuedEvent(val queueId: UUID,
                              val orderId: UUID)