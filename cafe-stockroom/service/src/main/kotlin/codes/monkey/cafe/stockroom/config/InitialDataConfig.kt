package codes.monkey.cafe.stockroom.config

import codes.monkey.cafe.stockroom.domain.model.CreateStockroomCommand
import codes.monkey.cafe.stockroom.domain.model.Item
import codes.monkey.cafe.stockroom.domain.model.StockroomConstants
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener

@Configuration
class InitialDataConfig {

    @Autowired
    lateinit var commandGateway: CommandGateway

    @EventListener
    fun createInitialStockRoom(contextStartedEvent: ContextRefreshedEvent) {
        commandGateway.sendAndWait<Any>(CreateStockroomCommand(
                id = StockroomConstants.STOCKROOM_ID,
                initialStock = listOf(
                        Item(name = "burger", quantity = 5)
                )
        ))
    }
}