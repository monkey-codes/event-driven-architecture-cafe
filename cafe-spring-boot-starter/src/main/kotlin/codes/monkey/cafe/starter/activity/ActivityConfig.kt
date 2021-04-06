package codes.monkey.cafe.starter.activity

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = ["codes.monkey.cafe.starter.activity"], excludeFilters = [ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*TestConfig.*"])])
class ActivityConfig {
}