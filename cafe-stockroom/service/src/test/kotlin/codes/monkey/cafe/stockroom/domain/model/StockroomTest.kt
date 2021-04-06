package codes.monkey.cafe.stockroom.domain.model

import codes.monkey.cafe.stockroom.domain.model.TestDataFactory.item
import codes.monkey.cafe.stockroom.domain.model.TestDataFactory.reStockStockroomCommand
import codes.monkey.cafe.stockroom.domain.model.TestDataFactory.stockroomCreatedEvent
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StockroomTest {
    private lateinit var fixture: AggregateTestFixture<Stockroom>

    @BeforeEach
    fun setup() {
        fixture = AggregateTestFixture(Stockroom::class.java)
    }

    @Test
    fun `should create a stockroom with initial stock`() {
        val createStockroomCommand = TestDataFactory.createStockroomCommand()
        fixture
                .givenNoPriorActivity()
                .`when`(createStockroomCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        StockroomCreatedEvent(
                                id = createStockroomCommand.id,
                                initialStock = createStockroomCommand.initialStock
                        )
                )
    }

    @Test
    fun `should update stock levels when item preparation starts`() {
        val useStockCommand = TestDataFactory.useStockCommand()
        fixture
                .given(stockroomCreatedEvent(initialStock = listOf(item(quantity = 10))))
                .`when`(useStockCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        StockUsedEvent(
                                id = useStockCommand.id,
                                stock = useStockCommand.stock
                        )
                )
    }

    @Test
    fun `should notify when out of stock`() {
        val useStockCommand = TestDataFactory.useStockCommand()
        fixture
                .given(stockroomCreatedEvent(initialStock = listOf(item(quantity = 1))))
                .`when`(useStockCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        StockUsedEvent(
                                id = useStockCommand.id,
                                stock = useStockCommand.stock
                        ),
                        OutOfStockEvent(
                                id = useStockCommand.id,
                                stock = useStockCommand.stock.map { it.name }
                        )
                )
    }

    @Test
    fun `should restock the stockroom`(){
        val reStockStockroomCommand = reStockStockroomCommand()
        fixture
                .given(stockroomCreatedEvent(initialStock = listOf(item(quantity = 0))))
                .`when`(reStockStockroomCommand)
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        StockroomReStockedEvent(
                                id = reStockStockroomCommand.id,
                                newStock = reStockStockroomCommand.newStock
                        )
                )
    }

}