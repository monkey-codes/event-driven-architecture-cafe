package codes.monkey.cafe.waiter.domain.model

import codes.monkey.cafe.waiter.api.commands.Item
import org.axonframework.modelling.command.EntityId
import java.util.*

class Order(@EntityId val orderId: UUID, val items: List<Item>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        if (orderId != other.orderId) return false

        return true
    }

    override fun hashCode(): Int {
        return orderId.hashCode()
    }
}