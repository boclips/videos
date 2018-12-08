package com.boclips.videos.service.config.security

import com.boclips.security.HttpSecurityConfigurer
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Component
class VideoServiceHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        http
                .cors()
                .configurationSource(corsConfiguration())

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                .antMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()

                .antMatchers("/v1").permitAll()
                .antMatchers("/v1/").permitAll()

                .antMatchers(HttpMethod.GET, "/v1/interactions").permitAll()
                .antMatchers(HttpMethod.GET, "/v1/events/status").permitAll()
                .antMatchers(HttpMethod.GET, "/v1/events/*").hasRole(UserRoles.REPORTING)
                .antMatchers(HttpMethod.POST, "/v1/events/*").hasAnyRole(UserRoles.TEACHER, UserRoles.REPORTING)

                .antMatchers(HttpMethod.POST, "/v1/admin/actions/rebuild_search_index").hasRole(UserRoles.REBUILD_SEARCH_INDEX)

                .antMatchers(HttpMethod.DELETE, "/v1/videos/*").hasRole(UserRoles.REMOVE_VIDEOS)
                .antMatchers(HttpMethod.POST, "/v1/videos").hasRole(UserRoles.INSERT_VIDEOS)
                .antMatchers(HttpMethod.GET, "/v1/videos*").hasAnyRole(UserRoles.TEACHER, UserRoles.VIEW_VIDEOS)
                .antMatchers(HttpMethod.GET, "/v1/videos/*").permitAll()
    }

    private fun corsConfiguration(): CorsConfigurationSource? {
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.addAllowedOrigin("http://localhost:8081")
        config.addAllowedOrigin("https://educators.staging-boclips.com")
        config.addAllowedOrigin("https://educators.testing-boclips.com")
        config.addAllowedOrigin("https://educators.boclips.com")
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}

