package codes.monkey.cafe.starter.leaderelection

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory
import org.springframework.integration.leader.Context

class LeadershipContext(private val context: Context) {
    fun yield() {
        context.yield()
    }

    fun isLeader() = context.isLeader
}

interface LeadershipListener {

    fun onLeadershipGranted(context: LeadershipContext)

    fun onLeadershipRevoked(context: LeadershipContext)
}

abstract class LeadershipListenerSupport : LeadershipListener {

    private val isLeader = AtomicBoolean(false)

    fun leaderOnly(callback: () -> Unit) {
        val logger = LoggerFactory.getLogger(javaClass)
        if (isLeader.get()) {
            logger.info("Executing listener ${javaClass.simpleName} on ${InetAddress.getLocalHost().hostAddress}")
            callback()
        } else {
            logger.info("Not the leader, ignoring ${javaClass.simpleName}")
        }
    }

    override fun onLeadershipRevoked(context: LeadershipContext) {
        isLeader.set(false)
    }

    override fun onLeadershipGranted(context: LeadershipContext) {
        isLeader.set(true)
    }
}
