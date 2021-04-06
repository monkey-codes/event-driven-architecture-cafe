package codes.monkey.cafe.waiter.events

import codes.monkey.cafe.waiter.api.commands.Item
import codes.monkey.cafe.starter.feed.PublicEvent
import java.util.*

data class OrderTakenEvent(
    val waiterId: UUID,
    val orderId: UUID,
    val items: List<Item>
) : PublicEvent