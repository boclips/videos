package com.boclips.videos.service.config

import com.boclips.security.EnableBoclipsSecurity
import com.boclips.security.HttpSecurityConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Profile("!test")
@Configuration
@EnableBoclipsSecurity
class WebSecurityConfig  {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:8080")
            }
        }
    }
}

object UserRoles {
    const val TEACHER = "TEACHER"
    const val REMOVE_VIDEOS = "REMOVE_VIDEOS"
    const val REMOVE_SEARCH_INDEX = "REMOVE_SEARCH_INDEX"
}

@Component
class VideoIngestorHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers("/v1").permitAll()
                .antMatchers("/v1/").permitAll()
                .antMatchers("/v1/interactions").permitAll()
                .antMatchers(HttpMethod.GET, "/v1/events/status").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/v1/videos/*").hasRole(UserRoles.REMOVE_VIDEOS)
                .antMatchers(HttpMethod.POST, "/v1/videos/rebuildSearchIndex").hasRole(UserRoles.REMOVE_SEARCH_INDEX)
                .anyRequest().hasRole(UserRoles.TEACHER)
    }
}

