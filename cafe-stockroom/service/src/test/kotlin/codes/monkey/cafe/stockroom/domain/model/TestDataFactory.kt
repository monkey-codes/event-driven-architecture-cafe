package codes.monkey.cafe.stockroom.domain.model

import java.util.*

object TestDataFactory {

    fun createStockroomCommand(id: UUID = StockroomConstants.STOCKROOM_ID,
                               initialStock: List<Item> = listOf(item())) =
            CreateStockroomCommand(
                    id = id,
                    initialStock = initialStock
            )

    fun reStockStockroomCommand(id: UUID = StockroomConstants.STOCKROOM_ID,
                                newStock: List<Item> = listOf(item())) =
            ReStockStockroomCommand(
                    id = id,
                    newStock = newStock
            )

    fun useStockCommand(id: UUID = StockroomConstants.STOCKROOM_ID,
                        stock: List<Item> = listOf(item())) =
            UseStockCommand(
                    id = id,
                    stock = stock
            )

    fun stockroomCreatedEvent(id: UUID = StockroomConstants.STOCKROOM_ID,
                              initialStock: List<Item> = listOf(item())) = StockroomCreatedEvent(
            id = id,
            initialStock = initialStock
    )


    fun stockroomReStockedEvent(id: UUID = StockroomConstants.STOCKROOM_ID,
                              newStock: List<Item> = listOf(item())) = StockroomReStockedEvent(
            id = id,
            newStock = newStock
    )

    fun item(name: String = "burger",
             quantity: Int = 1) = Item(
            name = name,
            quantity = quantity
    )
}