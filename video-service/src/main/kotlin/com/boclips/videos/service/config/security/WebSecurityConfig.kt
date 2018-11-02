package com.boclips.videos.service.config.security

import com.boclips.security.HttpSecurityConfigurer
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component


@Component
class VideoServiceHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers("/v1").permitAll()
                .antMatchers("/v1/").permitAll()
                .antMatchers("/v1/interactions").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()
                .antMatchers(HttpMethod.GET, "/v1/events/status").permitAll()
                .antMatchers(HttpMethod.GET, "/v1/events/*").hasRole(UserRoles.REPORTING)
                .antMatchers(HttpMethod.POST, "/v1/events/*").hasAnyRole(UserRoles.TEACHER, UserRoles.REPORTING)
                .antMatchers(HttpMethod.DELETE, "/v1/videos/*").hasRole(UserRoles.REMOVE_VIDEOS)
                .antMatchers(HttpMethod.POST, "/v1/videos/rebuildSearchIndex").hasRole(UserRoles.REBUILD_SEARCH_INDEX)
                .antMatchers(HttpMethod.GET, "/v1/videos/search").hasRole(UserRoles.TEACHER)
                .antMatchers(HttpMethod.GET, "/v1/videos/*").permitAll()
                .anyRequest().hasRole(UserRoles.TEACHER)
    }
}

