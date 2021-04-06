package codes.monkey.cafe.stockroom.config

import codes.monkey.cafe.starter.EnableActivityAPI
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableActivityAPI
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
       registry.addMapping("/**")
               .allowedOrigins(
                        "http://localhost:4200",
               )
    }
}