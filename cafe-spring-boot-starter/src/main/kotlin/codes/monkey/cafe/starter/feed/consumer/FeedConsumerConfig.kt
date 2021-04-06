package codes.monkey.cafe.starter.feed.consumer

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType.REGEX
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator
import org.springframework.scheduling.TaskScheduler

@Configuration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = [Filter(type = REGEX, pattern = [".*TestConfig.*"])])
class FeedConsumerConfig {

    @ConditionalOnMissingBean(LockRegistryLeaderInitiator::class)
    @Bean
    fun feedConsumerScheduler(
            feedConsumer: FeedConsumer,
            feedPositionRepository: FeedPositionRepository,
            taskScheduler: TaskScheduler,
            feedListener: FeedListener,
            meterRegistry: MeterRegistry,
            streamPreProcessors: List<StreamPreProcessor>
    ) =
            FeedConsumerScheduler(feedConsumer, feedPositionRepository, taskScheduler, feedListener, meterRegistry, streamPreProcessors)

    @ConditionalOnBean(LockRegistryLeaderInitiator::class)
    @Bean
    fun leaderOnlyFeedConsumerScheduler(
            feedConsumer: FeedConsumer,
            feedPositionRepository: FeedPositionRepository,
            taskScheduler: TaskScheduler,
            feedListener: FeedListener,
            meterRegistry: MeterRegistry,
            streamPreProcessors: List<StreamPreProcessor>
    ) =
            LeaderOnlyFeedConsumerScheduler(feedConsumer, feedPositionRepository, taskScheduler, feedListener, meterRegistry, streamPreProcessors)
}
