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
            .antMatchers(HttpMethod.POST, "/v1/events/*").permitAll()

            .antMatchers(HttpMethod.GET, "/v1/subjects").permitAll()
            .antMatchers(HttpMethod.POST, "/v1/subjects")
            .hasRole(UserRoles.CREATE_SUBJECT)

            .antMatchers(HttpMethod.POST, "/v1/admin/actions/rebuild_search_index")
            .hasRole(UserRoles.REBUILD_SEARCH_INDEX)
            .antMatchers(HttpMethod.POST, "/v1/admin/actions/build_legacy_search_index")
            .hasRole(UserRoles.REBUILD_SEARCH_INDEX)
            .antMatchers(HttpMethod.POST, "/v1/admin/actions/analyse_video/*")
            .hasRole(UserRoles.UPDATE_VIDEOS)
            .antMatchers(HttpMethod.POST, "/v1/admin/actions/analyse_videos")
            .hasRole(UserRoles.UPDATE_VIDEOS)

            .antMatchers(HttpMethod.POST, "/v1/admin/actions/refresh_playbacks")
            .hasRole(UserRoles.UPDATE_VIDEOS)

            .antMatchers(HttpMethod.POST, "/v1/e2e/actions/reset_all")
            .hasAnyRole(UserRoles.E2E)

            .antMatchers(HttpMethod.DELETE, "/v1/videos/*")
            .hasRole(UserRoles.REMOVE_VIDEOS)
            .antMatchers(HttpMethod.POST, "/v1/videos")
            .hasRole(UserRoles.INSERT_VIDEOS)
            .antMatchers(HttpMethod.PATCH, "/v1/videos")
            .hasRole(UserRoles.UPDATE_VIDEOS)
            .antMatchers(HttpMethod.POST, "/v1/videos/search")
            .hasRole(UserRoles.VIEW_DISABLED_VIDEOS)
            .antMatchers(HttpMethod.POST, "/v1/videos/*")
            .hasRole(UserRoles.UPDATE_VIDEOS)
            .antMatchers(HttpMethod.GET, "/v1/videos*")
            .hasAnyRole(UserRoles.VIEW_VIDEOS)
            .antMatchers(HttpMethod.GET, "/v1/videos/*/transcript")
            .hasAnyRole(UserRoles.DOWNLOAD_TRANSCRIPT)
            .antMatchers(HttpMethod.GET, "/v1/videos/*").permitAll()

            .antMatchers(HttpMethod.POST, "/v1/collections")
            .hasRole(UserRoles.INSERT_COLLECTIONS)
            .antMatchers(HttpMethod.GET, "/v1/collections")
            .hasRole(UserRoles.VIEW_COLLECTIONS)
            .antMatchers(HttpMethod.GET, "/v1/collections/*")
            .hasRole(UserRoles.VIEW_COLLECTIONS)
            .antMatchers(HttpMethod.PATCH, "/v1/collections/*")
            .hasRole(UserRoles.UPDATE_COLLECTIONS)
            .antMatchers(HttpMethod.DELETE, "/v1/collections/*")
            .hasRole(UserRoles.DELETE_COLLECTIONS)
            .antMatchers(HttpMethod.PUT, "/v1/collections/*/videos/*")
            .hasRole(UserRoles.UPDATE_COLLECTIONS)
            .antMatchers(HttpMethod.DELETE, "/v1/collections/*/videos/*")
            .hasRole(UserRoles.UPDATE_COLLECTIONS)

            .antMatchers(HttpMethod.HEAD, "/v1/content-partners/*/videos/*")
            .hasAnyRole(UserRoles.INSERT_VIDEOS)
            .antMatchers(HttpMethod.POST, "/v1/content-partners")
            .hasRole(UserRoles.CREATE_CONTENT_PARTNER)
            .antMatchers(HttpMethod.PUT, "/v1/content-partners/*")
            .hasRole(UserRoles.UPDATE_CONTENT_PARTNER)

            .anyRequest().denyAll()
    }
}


