package codes.monkey.cafe.kitchen.domain.model

import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*

internal class CookTest {

    private lateinit var fixture: AggregateTestFixture<Cook>

    @BeforeEach
    fun setup() {
        fixture = AggregateTestFixture(Cook::class.java)
    }

    @Test
    fun `should start meal preparation`() {
        val prepareOrderCommand = PrepareOrderCommand(cookId = KitchenConstants.COOK_ID, orderId = UUID.randomUUID(),
                waiterId = UUID.randomUUID(), items = listOf(Item(name = "burger", quantity = 1)))
        fixture
                .givenNoPriorActivity()
                .`when`(prepareOrderCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        OrderPreparationStartedEvent(
                                cookId = prepareOrderCommand.cookId,
                                orderId = prepareOrderCommand.orderId,
                                waiterId = prepareOrderCommand.waiterId,
                                items = prepareOrderCommand.items,
                        )
                )
    }

    @Test
    fun `should reject the order preparation command if an order is already in progress`() {
        val prepareOrderCommand = TestDataFactory.prepareOrderCommand()
        fixture
                .given(OrderPreparationStartedEvent(
                        cookId = prepareOrderCommand.cookId,
                        orderId = prepareOrderCommand.orderId,
                        waiterId = prepareOrderCommand.waiterId,
                        items = prepareOrderCommand.items,
                ))
                .`when`(prepareOrderCommand.copy(orderId = UUID.randomUUID()))
                .expectException(AlreadyBusyWithAnotherOrderException::class.java)

    }

    @Test
    fun `should notify when order preparation is complete`() {
        val prepareOrderCommand = TestDataFactory.prepareOrderCommand()
        fixture
                .givenCommands(prepareOrderCommand)
                .whenThenTimeElapses(Duration.ofSeconds(15))
                .expectEvents(
                        OrderPreparationCompletedEvent(
                                cookId = prepareOrderCommand.cookId,
                                orderId = prepareOrderCommand.orderId,
                                waiterId = prepareOrderCommand.waiterId,
                        )
                )

    }
}