package codes.monkey.cafe.cashier.events

import codes.monkey.cafe.starter.feed.PublicEvent
import java.util.*

data class BillItem(val name: String, val quantity: Int, val total: Int)
data class BillCreatedEvent(
        val id: UUID,
        val orderId: UUID,
        val items: List<BillItem>
): PublicEvent