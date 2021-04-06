package codes.monkey.cafe.cashier.domain.model

import java.lang.IllegalStateException

object Pricing {
    fun total(item: Item) =
            when (item.name) {
                "burger" -> item.quantity * 10
                "coffee" -> item.quantity * 3
                else -> throw IllegalStateException("Unkown item ${item.name}")
            }
}