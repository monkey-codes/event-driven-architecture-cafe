package codes.monkey.cafe.waiter.adapters.api.commands

import codes.monkey.cafe.waiter.api.IdResponse
import codes.monkey.cafe.waiter.api.commands.HireWaiterCommandRequest
import codes.monkey.cafe.waiter.api.commands.TakeOrderCommandRequest
import codes.monkey.cafe.waiter.domain.model.HireWaiterCommand
import codes.monkey.cafe.waiter.domain.model.TakeOrderCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/waiters")
class WaiterController(val commandGateway: CommandGateway) {

    @PostMapping
    fun hireWaiter(@RequestBody request: HireWaiterCommandRequest):
            CompletableFuture<ResponseEntity<IdResponse<UUID>>> = commandGateway
            .send<UUID>(HireWaiterCommand(id = request.id, name = request.name))
            .thenApply {
                ok().body(IdResponse(it))
            }

    @PostMapping("/{id}/orders")
    fun takeOrder(@PathVariable id: UUID, @RequestBody request: TakeOrderCommandRequest):
            CompletableFuture<ResponseEntity<IdResponse<UUID>>> = commandGateway
                    .send<UUID>(TakeOrderCommand(waiterId = id, orderId = request.id,  items = request.items))
                    .thenApply {
                        ok().body(IdResponse(it))
                    }
}