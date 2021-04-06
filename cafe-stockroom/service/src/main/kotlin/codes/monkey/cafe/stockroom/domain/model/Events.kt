package codes.monkey.cafe.stockroom.domain.model

import java.util.*

data class Item(val name: String, val quantity: Int)
data class StockroomCreatedEvent(val id: UUID, val initialStock: List<Item>)
data class StockroomReStockedEvent(val id: UUID, val newStock: List<Item>)

data class StockUsedEvent(
        val id: UUID,
        val stock: List<Item>
)

data class OutOfStockEvent(
        val id: UUID,
        val stock: List<String>
)