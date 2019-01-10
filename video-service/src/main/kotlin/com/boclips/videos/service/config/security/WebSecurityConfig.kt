package com.boclips.videos.service.config.security

import com.boclips.security.EnableBoclipsSecurity
import com.boclips.security.HttpSecurityConfigurer
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component

@Profile("!test")
@Configuration
@EnableBoclipsSecurity
class WebSecurityConfig

@Component
class VideoServiceHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()

                .antMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()

                .antMatchers("/v1").permitAll()
                .antMatchers("/v1/").permitAll()

                .antMatchers(HttpMethod.GET, "/v1/interactions").permitAll()
                .antMatchers(HttpMethod.GET, "/v1/events/status").permitAll()
                .antMatchers(HttpMethod.GET, "/v1/events/*").hasRole(UserRoles.VIEW_EVENTS)
                .antMatchers(HttpMethod.POST, "/v1/events/*").hasAnyRole(UserRoles.INSERT_EVENTS)

                .antMatchers(HttpMethod.POST, "/v1/admin/actions/rebuild_search_index").hasRole(UserRoles.REBUILD_SEARCH_INDEX)

                .antMatchers(HttpMethod.DELETE, "/v1/videos/*").hasRole(UserRoles.REMOVE_VIDEOS)
                .antMatchers(HttpMethod.POST, "/v1/videos").hasRole(UserRoles.INSERT_VIDEOS)
                .antMatchers(HttpMethod.POST, "/v1/videos/*").hasRole(UserRoles.UPDATE_VIDEOS)
                .antMatchers(HttpMethod.GET, "/v1/videos*").hasAnyRole(UserRoles.VIEW_VIDEOS)
                .antMatchers(HttpMethod.GET, "/v1/videos/*").permitAll()

                .antMatchers(HttpMethod.HEAD, "/v1/content-partners/*/videos/*").hasAnyRole(UserRoles.INSERT_VIDEOS)

                .anyRequest().denyAll()
    }
}

