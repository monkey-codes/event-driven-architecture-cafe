package codes.monkey.cafe.waiter.config

import codes.monkey.cafe.waiter.domain.model.HireWaiterCommand
import codes.monkey.cafe.waiter.domain.model.WaiterConstants
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
        commandGateway.sendAndWait<Any>(HireWaiterCommand(
                id = WaiterConstants.WAITER_ID,
                name = "monkey codes"
        ))
    }
}