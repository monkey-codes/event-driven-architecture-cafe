package codes.monkey.cafe.starter.leaderelection

import java.net.InetAddress
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.jdbc.lock.DefaultLockRepository
import org.springframework.integration.jdbc.lock.JdbcLockRegistry
import org.springframework.integration.jdbc.lock.LockRepository
import org.springframework.integration.leader.Candidate
import org.springframework.integration.leader.Context
import org.springframework.integration.leader.DefaultCandidate
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator
import org.springframework.integration.support.locks.LockRegistry

@Configuration
class LeaderElectionConfig {

    @Bean
    // do nothing, used in case no other listeners are registered
    fun noopLeadershipListener(): LeadershipListener {
        return object : LeadershipListener {
            override fun onLeadershipRevoked(context: LeadershipContext) {
                // no-op
            }

            override fun onLeadershipGranted(context: LeadershipContext) {
                // no-op
            }
        }
    }

    @Bean
    fun candidate(listeners: List<LeadershipListener>): Candidate {
        return ObservableCandidate(listeners)
    }

    @Bean
    fun lockRegistry(lockRepository: LockRepository): LockRegistry {
        return JdbcLockRegistry(lockRepository)
    }

    @Bean
    fun lockRepository(dataSource: DataSource): DefaultLockRepository {
        return DefaultLockRepository(dataSource)
    }

    @Bean
    fun leaderInitiator(lockRegistry: LockRegistry, candidate: Candidate): LockRegistryLeaderInitiator {
        val lockRegistryLeaderInitiator = LockRegistryLeaderInitiator(lockRegistry, candidate)
        lockRegistryLeaderInitiator.start()
        return lockRegistryLeaderInitiator
    }
}

class ObservableCandidate(private val listeners: List<LeadershipListener>) : DefaultCandidate() {

    companion object {
        val LOGGER = LoggerFactory.getLogger(LeaderElectionConfig::class.java)!!
    }

    override fun onGranted(ctx: Context) {
        LOGGER.info("Leadership granted to ${InetAddress.getLocalHost().hostAddress}")
        val leadershipContext = LeadershipContext(ctx)
        listeners.forEach {
            notifySafely { it.onLeadershipGranted(leadershipContext) }
        }
    }

    override fun onRevoked(ctx: Context) {
        LOGGER.info("Leadership revoked from ${InetAddress.getLocalHost().hostAddress}")
        val leadershipContext = LeadershipContext(ctx)
        listeners.forEach {
            notifySafely { it.onLeadershipRevoked(leadershipContext) }
        }
    }

    @Suppress("TooGenericExceptionCaught") // Ignore: Generic utility method
    private fun notifySafely(action: () -> Unit) {
        try {
            action()
        } catch (t: Throwable) {
            LOGGER.warn("Failed to notify LeadershipListener", t)
        }
    }
}
