package codes.monkey.cafe.waiter.adapters.api.commands

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import codes.monkey.cafe.starter.feed.producer.FeedController
import codes.monkey.cafe.test.MockMvcFeignClient
import codes.monkey.cafe.test.asyncDispatch
import codes.monkey.cafe.waiter.api.WaiterClient
import codes.monkey.cafe.waiter.api.commands.HireWaiterCommandRequest
import codes.monkey.cafe.waiter.api.commands.Item
import codes.monkey.cafe.waiter.api.commands.TakeOrderCommandRequest
import codes.monkey.cafe.waiter.domain.model.HireWaiterCommand
import codes.monkey.cafe.waiter.domain.model.TakeOrderCommand
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Client
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*
import java.util.concurrent.CompletableFuture.completedFuture


@ExtendWith(MockKExtension::class)
@WebMvcTest(controllers = [WaiterController::class])
internal class WaiterControllerTest {

    @MockBean
    lateinit var feedController: FeedController

    @TestConfiguration
    class TestConfig {
        @Bean
        fun commandGatewayMock(): CommandGateway = mockk(relaxed = true)

        @Bean
        fun queryGatewayMock(): QueryGateway = mockk(relaxed = true)

        @Bean
        fun feignClient(mockMvc: MockMvc): Client {
            return MockMvcFeignClient(mockMvc)
        }

        @Bean
        fun waiterClient(client: Client, objectMapper: ObjectMapper): WaiterClient {
            return Feign.builder()
                    .client(client)
                    .encoder(JacksonEncoder(objectMapper))
                    .decoder(JacksonDecoder(objectMapper))
                    .target(WaiterClient::class.java, "http://localhost:8080")
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var commandGateway: CommandGateway

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var waiterClient: WaiterClient

    val testOrderId = UUID.randomUUID()

    val testWaiterId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        every { commandGateway.send<UUID>(ofType(HireWaiterCommand::class )) } returns completedFuture(testWaiterId)
        every { commandGateway.send<UUID>(ofType(TakeOrderCommand::class))} returns completedFuture(testOrderId)
    }

    @AfterEach
    fun tearDown(){
        clearMocks(commandGateway)
    }

    @Test
    fun `should be able to add waiters to the payroll`() {

        mockMvc.asyncDispatch {
            post("/api/waiters")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                            jsonString(hireWaiterCommandRequest())
                    )
        }.andExpect(jsonPath("$.id", `is`(testWaiterId.toString())))
        verify { commandGateway.send<UUID>(withArg<HireWaiterCommand> { assertThat(it.id).isEqualTo(testWaiterId) }) }
    }

    @Test
    fun `should be able to add waiters via the client`() {
        val result = waiterClient.hireWaiter(hireWaiterCommandRequest())
        assertThat(result.id).isEqualTo(testWaiterId);
    }

    @Test
    fun `should be able to take orders`() {
        mockMvc.asyncDispatch {
            post("/api/waiters/${testWaiterId}/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                            jsonString(takeOrderCommandRequest())
                    )
        }.andExpect(jsonPath("$.id", `is`(testOrderId.toString())))
        verify { commandGateway.send<UUID>(withArg<TakeOrderCommand> {
            assertThat(it.waiterId).isEqualTo(testWaiterId)
            assertThat(it.orderId).isEqualTo(testOrderId)
            assertThat(it.items).hasSize(1)
        }) }

    }

    @Test
    fun `should be able to take orders via the client`() {
        val result = waiterClient.takeOrder(testWaiterId.toString(), takeOrderCommandRequest())
        assertThat(result.id).isEqualTo(testOrderId);
    }

    private fun hireWaiterCommandRequest() = HireWaiterCommandRequest(id = testWaiterId, name = "chuck norris")

    private fun takeOrderCommandRequest() = TakeOrderCommandRequest(
            id = testOrderId,
            items = listOf(Item(name = "burger", quantity = 1))
    )

    private fun jsonString(obj: Any) =
            objectMapper.writeValueAsString(obj)
}

