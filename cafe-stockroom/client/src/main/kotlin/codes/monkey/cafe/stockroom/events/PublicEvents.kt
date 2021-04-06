package codes.monkey.cafe.stockroom.events

import codes.monkey.cafe.starter.feed.PublicEvent
import java.util.*

data class Item(val name: String, val quantity: Int)

data class StockroomReStockedEvent(val id: UUID,
                                   val newStock: List<Item>) : PublicEvent

data class OutOfStockEvent(
        val id: UUID,
        val stock: List<String>
) : PublicEvent