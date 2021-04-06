package codes.monkey.cafe.kitchen.domain.model

import codes.monkey.cafe.kitchen.domain.model.KitchenConstants.COOK_ID
import codes.monkey.cafe.kitchen.domain.model.KitchenConstants.ORDER_QUEUE_ID
import codes.monkey.cafe.kitchen.domain.model.TestDataFactory.orderPreparationCompletedEvent
import codes.monkey.cafe.kitchen.domain.model.TestDataFactory.orderPreparationStartedEvent
import codes.monkey.cafe.kitchen.domain.model.TestDataFactory.orderQueuedEvent
import org.axonframework.test.saga.SagaTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration.ofMinutes
import java.util.*

internal class OrderPreparationSagaTest {

    private lateinit var fixture: SagaTestFixture<OrderPreparationSaga>

    @BeforeEach
    fun setup() {
        fixture = SagaTestFixture(OrderPreparationSaga::class.java)
    }

    @Test
    fun `should start food preparation saga on OrderQueuedEvent`() {
        val orderQueuedEvent = orderQueuedEvent()
        fixture
                .givenNoPriorActivity()
                .whenAggregate(ORDER_QUEUE_ID.toString()).publishes(orderQueuedEvent)
                .expectActiveSagas(1)
                .expectAssociationWith("cookId", COOK_ID)
                .expectDispatchedCommands(
                        PrepareOrderCommand(
                                cookId = COOK_ID,
                                orderId = orderQueuedEvent.orderId,
                                waiterId = orderQueuedEvent.waiterId,
                                items = orderQueuedEvent.items
                        )
                )
    }

    @Test
    fun `should wait to prepare order if cook is already busy`(){
        fixture.setCallbackBehavior { any, metaData ->
            throw AlreadyBusyWithAnotherOrderException(orderId = UUID.randomUUID())
        }
        val orderQueuedEvent = orderQueuedEvent()
        fixture
                .givenNoPriorActivity()
                .whenAggregate(ORDER_QUEUE_ID.toString()).publishes(orderQueuedEvent)
                .expectActiveSagas(1)
                .expectAssociationWith("cookId", COOK_ID)
                .expectDispatchedCommands(
                        PrepareOrderCommand(
                                cookId = COOK_ID,
                                orderId = orderQueuedEvent.orderId,
                                waiterId = orderQueuedEvent.waiterId,
                                items = orderQueuedEvent.items
                        )
                )
                .expectScheduledDeadlineWithName(ofMinutes(10), "orderQueuedTooLong")
    }

    @Test
    fun `should remove order from queue once preparation is complete`(){
        val orderId = UUID.randomUUID()
        val orderQueuedEvent = orderQueuedEvent(orderId = orderId)
        val orderPreparationStartedEvent  = orderPreparationStartedEvent(orderId = orderId)
        val orderPreparationCompletedEvent  = orderPreparationCompletedEvent(orderId = orderId)
        fixture
                .givenAggregate(ORDER_QUEUE_ID.toString()).published(orderQueuedEvent)
                .andThenAggregate(COOK_ID.toString()).published(orderPreparationStartedEvent)
                .whenAggregate(COOK_ID.toString()).publishes(orderPreparationCompletedEvent)
                .expectActiveSagas(1)
                .expectAssociationWith("cookId", COOK_ID)
                .expectDispatchedCommands(
                        DequeueOrderCommand(
                                queueId = ORDER_QUEUE_ID,
                                orderId = orderQueuedEvent.orderId
                        )
                )
    }

    @Test
    fun `should end the saga when order has been dequeued`(){
        val orderId = UUID.randomUUID()
        val orderQueuedEvent = orderQueuedEvent(orderId = orderId)
        val orderPreparationStartedEvent  = orderPreparationStartedEvent(orderId = orderId)
        val orderPreparationCompletedEvent  = orderPreparationCompletedEvent(orderId = orderId)
        fixture
                .givenAggregate(ORDER_QUEUE_ID.toString()).published(orderQueuedEvent)
                .andThenAggregate(COOK_ID.toString()).published(orderPreparationStartedEvent)
                .andThenAggregate(COOK_ID.toString()).published(orderPreparationCompletedEvent)
                .whenAggregate(ORDER_QUEUE_ID.toString()).publishes(OrderDequeuedEvent(queueId = ORDER_QUEUE_ID, orderId = orderId))
                .expectActiveSagas(0)
    }
}