package codes.monkey.cafe.stockroom.domain.model

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class CreateStockroomCommand(
        @TargetAggregateIdentifier
        val id: UUID,
        val initialStock: List<Item>
)

data class ReStockStockroomCommand(
        @TargetAggregateIdentifier
        val id: UUID,
        val newStock: List<Item>
)

data class UseStockCommand(
        @TargetAggregateIdentifier
        val id: UUID,
        val stock: List<Item>
)