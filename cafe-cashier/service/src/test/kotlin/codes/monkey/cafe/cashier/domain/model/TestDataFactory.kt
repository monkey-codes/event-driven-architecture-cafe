package codes.monkey.cafe.cashier.domain.model

import java.util.*

object TestDataFactory {

   fun item(name: String = "burger",
            quantity: Int = 1) = Item(
           name = name,
           quantity = quantity
   )

    fun createBillCommand(id: UUID = UUID.randomUUID(),
                          items: List<Item> = listOf(item()),
                          orderId: UUID = UUID.randomUUID()) = CreateBillCommand(
            id = id,
            orderId = orderId,
            items = items
    )
}