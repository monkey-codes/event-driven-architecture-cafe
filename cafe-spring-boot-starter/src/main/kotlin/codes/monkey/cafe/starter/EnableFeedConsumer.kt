package codes.monkey.cafe.starter

import codes.monkey.cafe.starter.feed.consumer.FeedConsumerConfig
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(FeedConsumerConfig::class)
annotation class EnableFeedConsumer
