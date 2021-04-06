package codes.monkey.cafe.kitchen.domain.model

import java.util.*

object TestDataFactory {

    fun orderQueuedEvent(queueId: UUID = KitchenConstants.ORDER_QUEUE_ID,
                         orderId: UUID = UUID.randomUUID(),
                         waiterId: UUID = UUID.randomUUID(),
                         items: List<Item> = listOf(item())) = OrderQueuedEvent(
            queueId = queueId,
            orderId = orderId,
            waiterId = waiterId,
            items = items,
    )

    fun item(name: String = "burger",
             quantity: Int = 1) = Item(
            name = name,
            quantity = quantity
    )

    fun orderPreparationStartedEvent(
            cookId: UUID = KitchenConstants.COOK_ID,
            orderId: UUID = UUID.randomUUID(),
            waiterId: UUID = UUID.randomUUID(),
            items: List<Item> = listOf(item())
    ) = OrderPreparationStartedEvent(
            cookId = cookId,
            orderId = orderId,
            waiterId = waiterId,
            items = items,
    )

    fun orderPreparationCompletedEvent(
            cookId: UUID = KitchenConstants.COOK_ID,
            orderId: UUID = UUID.randomUUID(),
            waiterId: UUID = UUID.randomUUID(),
    ) = OrderPreparationCompletedEvent(
            cookId = cookId,
            orderId = orderId,
            waiterId = waiterId,
    )

    fun prepareOrderCommand(cookId: UUID = KitchenConstants.COOK_ID,
                            orderId: UUID = UUID.randomUUID(),
                            waiterId: UUID = UUID.randomUUID(),
                            items: List<Item> = listOf(item())) = PrepareOrderCommand(cookId = cookId,
            orderId = orderId,
            waiterId = waiterId,
            items = items)

}