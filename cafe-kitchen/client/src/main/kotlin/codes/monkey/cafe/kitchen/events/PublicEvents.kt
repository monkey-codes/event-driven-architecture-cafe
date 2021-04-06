package codes.monkey.cafe.kitchen.events

import codes.monkey.cafe.starter.feed.PublicEvent
import java.util.*

data class OrderPreparationCompletedEvent(
        val cookId: UUID,
        val orderId: UUID,
        val waiterId: UUID,
): PublicEvent

data class Item(val name: String, val quantity: Int)

data class OrderPreparationStartedEvent(
        val cookId: UUID,
        val orderId: UUID,
        val waiterId: UUID,
        val items: List<Item>
): PublicEvent