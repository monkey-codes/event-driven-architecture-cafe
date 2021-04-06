package codes.monkey.cafe.cashier.domain.model

import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BillTest {
    private lateinit var fixture: AggregateTestFixture<Bill>

    @BeforeEach
    fun setup() {
        fixture = AggregateTestFixture(Bill::class.java)
    }

    @Test
    fun `should create a bill`() {
        val createBillCommand = TestDataFactory.createBillCommand()
        fixture
                .givenNoPriorActivity()
                .`when`(createBillCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        BillCreatedEvent(
                                id = createBillCommand.id,
                                orderId = createBillCommand.orderId,
                                items = createBillCommand.items.map { BillItem(name = it.name, quantity = it.quantity, Pricing.total(it)) }
                        )
                )
    }

}