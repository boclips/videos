package com.boclips.videos.service.config.security

import com.boclips.security.EnableBoclipsSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Profile("!test")
@Configuration
@EnableBoclipsSecurity
class WebSecurityConfig {
    @Bean
    fun corsConfigurer() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**")
                    .allowedMethods("GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS")
                    .allowedOrigins(
                            "http://localhost:8080",
                            "https://educators.staging-boclips.com",
                            "https://educators.testing-boclips.com",
                            "https://educators.boclips.com"
                    )
        }
    }
}