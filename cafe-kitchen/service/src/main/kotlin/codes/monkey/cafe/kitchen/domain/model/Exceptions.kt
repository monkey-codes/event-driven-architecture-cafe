package codes.monkey.cafe.kitchen.domain.model

import java.lang.Exception
import java.util.*

class AlreadyBusyWithAnotherOrderException(val orderId: UUID?) : Exception("orderId: $orderId")