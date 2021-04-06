package codes.monkey.cafe.waiter.domain.model

import codes.monkey.cafe.waiter.api.commands.Item
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class HireWaiterCommand(
        @TargetAggregateIdentifier
        val id: UUID,
        val name: String,
)

data class TakeOrderCommand(
        @TargetAggregateIdentifier
        val waiterId: UUID,
        val orderId: UUID,
        val items: List<Item>
)

data class DeliverOrderToTableCommand(
        @TargetAggregateIdentifier
        val waiterId: UUID,
        val orderId: UUID,
)

data class StopTakingOrdersForOutOfStockItemsCommand(
        @TargetAggregateIdentifier
        val waiterId: UUID,
        val items: List<String>
)

data class StartTakingOrdersForFreshlyStockedItemsCommand(
        @TargetAggregateIdentifier
        val waiterId: UUID,
        val items: List<String>
)