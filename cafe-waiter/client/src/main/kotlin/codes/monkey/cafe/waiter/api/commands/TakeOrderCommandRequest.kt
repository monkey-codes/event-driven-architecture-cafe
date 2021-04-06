package codes.monkey.cafe.waiter.api.commands

import java.util.*

data class Item(val name: String, val quantity: Int)

data class TakeOrderCommandRequest(val items: List<Item>, val id: UUID)