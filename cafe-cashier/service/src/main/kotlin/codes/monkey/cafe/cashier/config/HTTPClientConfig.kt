package codes.monkey.cafe.cashier.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestOperations
import java.time.Duration

@Configuration
class HTTPClientConfig {

    @Bean
    @Suppress("MagicNumber") // This should not be suppressed, but has been done so after fixing the Detekt plugin.
    fun restTemplate(
            builder: RestTemplateBuilder,
    ): RestOperations =
            builder.setConnectTimeout(Duration.ofSeconds(2))
                    .setReadTimeout(Duration.ofSeconds(2))
                    .build()

}