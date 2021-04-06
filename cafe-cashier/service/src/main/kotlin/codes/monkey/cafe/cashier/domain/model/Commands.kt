package codes.monkey.cafe.cashier.domain.model

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class Item(val name: String, val quantity: Int);
data class CreateBillCommand(@TargetAggregateIdentifier val id: UUID, val orderId: UUID, val items: List<Item>)