package codes.monkey.cafe.stockroom.config

import org.axonframework.config.ConfigurationScopeAwareProvider
import org.axonframework.deadline.DeadlineManager
import org.axonframework.deadline.SimpleDeadlineManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfiguration {

    @Bean
     fun deadlineManager(config: org.axonframework.config.Configuration?): DeadlineManager? {
        return SimpleDeadlineManager.builder().scopeAwareProvider(ConfigurationScopeAwareProvider(config)).build()
    }

}