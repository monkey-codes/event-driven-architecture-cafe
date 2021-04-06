package codes.monkey.cafe.kitchen.domain.model

import codes.monkey.cafe.kitchen.domain.model.KitchenConstants.ORDER_QUEUE_ID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class OrderQueueTest {
    private lateinit var fixture: AggregateTestFixture<OrderQueue>

    @BeforeEach
    fun setup() {
        fixture = AggregateTestFixture(OrderQueue::class.java)
    }

    @Test
    fun `should queue up orders taken by the waiter`() {
        val queueOrderCommand = QueueOrderCommand(orderId = UUID.randomUUID(),
                waiterId = UUID.randomUUID(), items = listOf(Item(name = "burger", quantity = 1)))
        fixture
                .givenNoPriorActivity()
                .`when`(queueOrderCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        OrderQueuedEvent(
                                queueId = queueOrderCommand.queueId,
                                orderId = queueOrderCommand.orderId,
                                waiterId = queueOrderCommand.waiterId,
                                items = queueOrderCommand.items,
                        )
                )
    }

    @Test
    fun `should remove completed orders from the queue`() {
        val orderQueuedEvent = TestDataFactory.orderQueuedEvent()
        fixture
                .given(orderQueuedEvent)
                .`when`(DequeueOrderCommand(orderId = orderQueuedEvent.orderId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        OrderDequeuedEvent(
                                queueId = ORDER_QUEUE_ID,
                                orderId = orderQueuedEvent.orderId
                        )
                )
    }
}