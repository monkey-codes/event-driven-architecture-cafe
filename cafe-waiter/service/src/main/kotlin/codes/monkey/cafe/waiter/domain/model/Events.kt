package codes.monkey.cafe.waiter.domain.model

import codes.monkey.cafe.waiter.api.commands.Item
import java.util.*

data class WaiterHiredEvent(val id: UUID, val name: String)
data class OrderTakenEvent(val waiterId: UUID, val orderId: UUID, val items: List<Item>)
data class OrderDeliveredEvent(val waiterId: UUID, val orderId: UUID)
data class ItemsMarkedOutOfStockEvent(val waiterId: UUID, val items: List<String>)
data class ItemsMarkedInStockEvent(val waiterId: UUID, val items: List<String>)
