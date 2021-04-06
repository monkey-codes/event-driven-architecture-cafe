package codes.monkey.cafe.starter.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logging {
    inline fun <reified T : Any> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)
}