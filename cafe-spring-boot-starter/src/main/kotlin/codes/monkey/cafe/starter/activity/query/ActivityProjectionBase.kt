package codes.monkey.cafe.starter.activity.query

import org.axonframework.eventhandling.EventMessage
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.*

//@Component
abstract class ActivityProjectionBase(
        val queryUpdateEmitter: QueryUpdateEmitter
) {

    var lastActivity: Activity = Activity(
            eventId = UUID.randomUUID(),
            entityId = UUID.randomUUID(),
            activity = "NOOP")


    protected fun emit(event: EventMessage<*>, entityId: UUID, activity: String) {
        lastActivity = Activity(
                eventId = UUID.fromString(event.identifier),
                entityId = entityId,
                activity = activity)
        queryUpdateEmitter.emit(
                GetRecentActivityQuery::class.java, { query -> true }, lastActivity
        )
    }

    @QueryHandler
    fun handle(query: GetRecentActivityQuery): Activity = lastActivity
}

data class Activity(val eventId: UUID, val entityId: UUID, val activity: String)