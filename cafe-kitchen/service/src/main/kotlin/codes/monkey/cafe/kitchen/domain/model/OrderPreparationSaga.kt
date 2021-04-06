package codes.monkey.cafe.kitchen.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.deadline.DeadlineManager
import org.axonframework.deadline.annotation.DeadlineHandler
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

@Saga
class OrderPreparationSaga {

    var orderInProgress: Boolean = false
    lateinit var order: Order

    @Autowired
    @JsonIgnore
    @Transient
    lateinit var commandGateway: CommandGateway

    @Autowired
    @JsonIgnore
    @Transient
    lateinit var deadlineManager: DeadlineManager

    var deadlineScheduleId: String? = null

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    fun on(event: OrderQueuedEvent) {
        order = Order(orderId = event.orderId, waiterId = event.waiterId, items = event.items)
        SagaLifecycle.associateWith("cookId", KitchenConstants.COOK_ID.toString())
        sendPrepareOrderCommand().exceptionally {
            if (it is AlreadyBusyWithAnotherOrderException) {
                deadlineScheduleId = deadlineManager.schedule(
                        Duration.ofMinutes(10),
                        "orderQueuedTooLong"
                )
            }
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    fun on(event: OrderPreparationStartedEvent) {
        this.orderInProgress = true
    }

    @SagaEventHandler(associationProperty = "cookId")
    fun on(event: OrderPreparationCompletedEvent) {
        if (!this.orderInProgress) {
            //this order is still queued, retry
            sendPrepareOrderCommand()
            return
        }
        if (event.orderId == order.orderId) {
            if (this.deadlineScheduleId != null) {
                this.deadlineManager.cancelSchedule("orderQueuedTooLong", deadlineScheduleId)
            }
            commandGateway.send<Any>(DequeueOrderCommand(orderId = order.orderId))
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    fun on(event: OrderDequeuedEvent) {
    }

    private fun sendPrepareOrderCommand() = commandGateway.send<Any>(PrepareOrderCommand(
            cookId = KitchenConstants.COOK_ID,
            orderId = order.orderId,
            waiterId = order.waiterId,
            items = order.items
    ))

    @DeadlineHandler(deadlineName = "orderQueuedTooLong")
    fun onOrderQueuedTooLongDeadline() {
        //TODO: refund the money? send back the waiter to apologize?
        // Offer complimentary drink?
    }
}