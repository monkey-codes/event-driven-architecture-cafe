package codes.monkey.cafe.waiter.domain.model

import codes.monkey.cafe.waiter.api.commands.Item
import java.util.*

object TestDataFactory {

    fun takeOrderCommand(waiterId: UUID = WaiterConstants.WAITER_ID,
                         orderId: UUID = UUID.randomUUID(),
                         items: List<Item> = listOf(item())) = TakeOrderCommand(
            waiterId = waiterId,
            orderId = orderId,
            items = items
    )

    fun item(name: String = "burger",
             quantity: Int = 1) = Item(
            name = name,
            quantity = quantity
    )

    fun waiterHiredEvent(waiterId: UUID = WaiterConstants.WAITER_ID,
                         name: String = "monkey codes") = WaiterHiredEvent(
            id = waiterId,
            name = name)

    fun itemsMarkedOutOfStockEvent(waiterId: UUID = WaiterConstants.WAITER_ID,
                                   items: List<String> = listOf("burger")) =
            ItemsMarkedOutOfStockEvent(waiterId = waiterId,
                    items = items)

    fun itemsMarkedInStockEvent(waiterId: UUID = WaiterConstants.WAITER_ID,
                                items: List<String> = listOf("burger")) =
            ItemsMarkedInStockEvent(waiterId = waiterId,
                    items = items)

    fun orderTakenEvent(waiterId: UUID = WaiterConstants.WAITER_ID,
                        orderId: UUID = UUID.randomUUID(),
                        items: List<Item> = listOf(item())) =
            OrderTakenEvent(waiterId = waiterId,
                    orderId = orderId,
                    items = items)

}