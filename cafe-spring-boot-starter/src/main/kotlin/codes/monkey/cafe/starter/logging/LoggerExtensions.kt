package codes.monkey.cafe.starter.logging

import org.axonframework.modelling.command.AggregateLifecycle
import org.slf4j.Logger

fun Logger.logEvent(event: Any) {
    try {
        if (AggregateLifecycle.isLive()) {
            info("Applying {}", event)
        } else {
            debug("Sourcing {}", event)
        }
    } catch (e: IllegalStateException) {
        info("Applying {}", event)
    }
}