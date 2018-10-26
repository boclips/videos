package com.boclips.backofficeapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ApiConfig {

    @Bean
    fun corsConfigurer() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**")
                    .allowedMethods("GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS")
                    .allowedOrigins(
                            "http://localhost:8081",
                            "https://educators.staging-boclips.com",
                            "https://educators.testing-boclips.com",
                            "https://educators.boclips.com"
                    )
        }
    }

}