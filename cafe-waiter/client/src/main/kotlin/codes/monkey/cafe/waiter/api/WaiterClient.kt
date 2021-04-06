package codes.monkey.cafe.waiter.api

import codes.monkey.cafe.waiter.api.commands.HireWaiterCommandRequest
import codes.monkey.cafe.waiter.api.commands.TakeOrderCommandRequest
import feign.Headers
import feign.Param
import feign.RequestLine
import java.util.*

interface WaiterClient {

    @RequestLine("POST /api/waiters")
    @Headers("Content-Type: application/json")
    fun hireWaiter(request: HireWaiterCommandRequest): IdResponse<UUID>

    @RequestLine("POST /api/waiters/{id}/orders")
    @Headers("Content-Type: application/json")
    fun takeOrder(@Param("id") testWaiterId: String, takeOrderCommandRequest: TakeOrderCommandRequest): IdResponse<UUID>
}