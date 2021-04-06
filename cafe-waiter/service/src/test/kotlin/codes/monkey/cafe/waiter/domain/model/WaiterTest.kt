package codes.monkey.cafe.waiter.domain.model

import codes.monkey.cafe.waiter.domain.model.TestDataFactory.itemsMarkedInStockEvent
import codes.monkey.cafe.waiter.domain.model.TestDataFactory.orderTakenEvent
import codes.monkey.cafe.waiter.domain.model.TestDataFactory.itemsMarkedOutOfStockEvent
import codes.monkey.cafe.waiter.domain.model.TestDataFactory.takeOrderCommand
import codes.monkey.cafe.waiter.domain.model.TestDataFactory.waiterHiredEvent
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class WaiterTest {


    private lateinit var fixture: AggregateTestFixture<Waiter>

    @BeforeEach
    fun setup() {
        fixture = AggregateTestFixture(Waiter::class.java)
    }

    @Test
    fun `should create a waiter aggregate when one is hired`() {
        val hireWaiterCommand = HireWaiterCommand(id = UUID.randomUUID(), name = "test")
        fixture
                .givenNoPriorActivity()
                .`when`(hireWaiterCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        WaiterHiredEvent(id = hireWaiterCommand.id, name = hireWaiterCommand.name)
                )
    }

    @Test
    fun `should take an order`() {
        val waiterId = UUID.randomUUID()
        val takeOrderCommand = takeOrderCommand(waiterId = waiterId)
        fixture
                .given(WaiterHiredEvent(id = waiterId, name = "monkey codes"))
                .`when`(takeOrderCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        OrderTakenEvent(waiterId = waiterId,
                                orderId = takeOrderCommand.orderId,
                                items = takeOrderCommand.items)
                )

    }

    @Test
    fun `should deliver prepared orders to the table`() {
        val waiterId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        fixture
                .given(
                        waiterHiredEvent(waiterId = waiterId),
                        orderTakenEvent(waiterId = waiterId, orderId = orderId)
                )
                .`when`(DeliverOrderToTableCommand(waiterId = waiterId, orderId = orderId))
                .expectEvents(
                        OrderDeliveredEvent(waiterId = waiterId, orderId = orderId)
                )
    }

    @Test
    fun `should mark out of stock items to stop future orders with those items`() {
        val waiterId = UUID.randomUUID()
        val stopTakingOrdersForOutOfStockItemsCommand = StopTakingOrdersForOutOfStockItemsCommand(waiterId = waiterId, items = listOf("burger"))
        fixture
                .given(
                        waiterHiredEvent(waiterId = waiterId)
                )
                .`when`(stopTakingOrdersForOutOfStockItemsCommand)
                .expectEvents(
                        ItemsMarkedOutOfStockEvent(waiterId = waiterId, items = stopTakingOrdersForOutOfStockItemsCommand.items)
                )
    }

    @Test
    fun `should not take orders for out of stock items`() {
        val takeOrderCommand = takeOrderCommand()
        fixture
                .given(
                        waiterHiredEvent(),
                        itemsMarkedOutOfStockEvent(),
                        itemsMarkedInStockEvent()
                )
                .`when`(takeOrderCommand)
                .expectSuccessfulHandlerExecution()
    }
}