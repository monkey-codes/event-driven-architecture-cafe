package codes.monkey.cafe.starter.feed.producer

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType.REGEX

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = ["codes.monkey.cafe.starter.feed.producer"], excludeFilters = [Filter(type = REGEX, pattern = [".*TestConfig.*"])])
class FeedProducerConfig
