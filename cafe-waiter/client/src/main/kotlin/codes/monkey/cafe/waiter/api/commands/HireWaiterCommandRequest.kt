package codes.monkey.cafe.waiter.api.commands

import java.util.*

data class HireWaiterCommandRequest(
        val id: UUID,
        val name: String,
)