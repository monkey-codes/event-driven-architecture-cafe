package codes.monkey.cafe.starter

import codes.monkey.cafe.starter.feed.producer.FeedProducerConfig
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(FeedProducerConfig::class)
annotation class EnableFeedProducer
