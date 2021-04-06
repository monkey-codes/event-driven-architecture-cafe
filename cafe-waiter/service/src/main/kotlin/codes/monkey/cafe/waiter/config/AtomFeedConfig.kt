package codes.monkey.cafe.waiter.config

import codes.monkey.cafe.starter.EnableFeedConsumer
import codes.monkey.cafe.starter.EnableFeedProducer
import codes.monkey.cafe.starter.EnableLeaderElection
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableLeaderElection
@EnableFeedConsumer
@EnableFeedProducer
@EnableScheduling
class AtomFeedConfig {
}