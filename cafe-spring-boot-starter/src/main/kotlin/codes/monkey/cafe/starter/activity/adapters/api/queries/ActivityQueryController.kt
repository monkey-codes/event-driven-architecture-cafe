package codes.monkey.cafe.starter.activity.adapters.api.queries

import codes.monkey.cafe.starter.activity.query.Activity
import codes.monkey.cafe.starter.activity.query.GetRecentActivityQuery
import codes.monkey.cafe.starter.logging.Logging
import org.axonframework.queryhandling.QueryGateway
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:4200"])
class ActivityQueryController(
        val queryGateway: QueryGateway
) {

    companion object {
        val LOGGER: Logger = Logging.loggerFor<ActivityQueryController>()
    }

    @GetMapping("/activity")
    fun activity(): ResponseEntity<SseEmitter> {
        val emitter = SseEmitter()
        val subscriptionQueryResult = queryGateway.subscriptionQuery(
                GetRecentActivityQuery(),
                Activity::class.java, Activity::class.java
        )
        subscriptionQueryResult.handle({ initial ->
            emitter.send(event()
                    .id(initial.eventId.toString())
                    .data(initial, MediaType.APPLICATION_JSON)
            )
        }, { update ->
            try {
                emitter.send(
                        event()
                                .id(update.eventId.toString())
                                .data(update, MediaType.APPLICATION_JSON)
                )
            } catch (e: Exception) {
                LOGGER.info("Failed to emit activity update ${e.message}")
                subscriptionQueryResult.close()
            }
        })
        emitter.onCompletion {
            LOGGER.info("Closing subscription query")
            subscriptionQueryResult.close()
        }

        val responseHeaders = HttpHeaders()
        responseHeaders.set("Access-Control-Allow-Credentials", "true")
        return ResponseEntity.ok().headers(responseHeaders).body(emitter);
    }
}