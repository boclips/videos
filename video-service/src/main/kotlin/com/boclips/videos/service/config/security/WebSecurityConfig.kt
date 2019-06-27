package com.boclips.videos.service.config.security

import com.boclips.security.EnableBoclipsSecurity
import com.boclips.security.HttpSecurityConfigurer
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.HEAD
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component
import com.boclips.videos.service.config.security.UserRoles as ROLE

@Profile("!test")
@Configuration
@EnableBoclipsSecurity
class WebSecurityConfig

@Component
class VideoServiceHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {

        http
            .authorizeRequests()
            .antMatchers(GET, "/actuator/health").permitAll()
            .antMatchers(GET, "/actuator/prometheus").permitAll()

            .antMatchers(OPTIONS, "/v1/**").permitAll()

            .antMatchers("/v1").permitAll()
            .antMatchers("/v1/").permitAll()

            .antMatchers(GET, "/v1/interactions").permitAll()
            .antMatchers(POST, "/v1/events/*").permitAll()

            .antMatchers(GET, "/v1/subjects").permitAll()
            .antMatchers(GET, "/v1/subjects/*").permitAll()
            .antMatchers(POST, "/v1/subjects").hasRole(ROLE.CREATE_SUBJECT)

            .antMatchers(GET, "/v1/disciplines").hasRole(ROLE.VIEW_DISCIPLINES)
            .antMatchers(GET, "/v1/disciplines/*").hasRole(ROLE.VIEW_DISCIPLINES)
            .antMatchers(PUT, "/v1/disciplines/*/subjects").hasRole(ROLE.UPDATE_DISCIPLINES)
            .antMatchers(POST, "/v1/disciplines").hasRole(ROLE.INSERT_DISCIPLINES)

            .antMatchers(POST, "/v1/admin/actions/rebuild_video_index").hasRole(ROLE.REBUILD_SEARCH_INDEX)
            .antMatchers(POST, "/v1/admin/actions/rebuild_collection_index").hasRole(ROLE.REBUILD_SEARCH_INDEX)
            .antMatchers(POST, "/v1/admin/actions/build_legacy_search_index").hasRole(ROLE.REBUILD_SEARCH_INDEX)
            .antMatchers(POST, "/v1/admin/actions/analyse_video/*").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(POST, "/v1/admin/actions/analyse_videos").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(POST, "/v1/admin/actions/classify_videos").hasRole(ROLE.UPDATE_VIDEOS)

            .antMatchers(POST, "/v1/admin/actions/refresh_playbacks").hasRole(ROLE.UPDATE_VIDEOS)

            .antMatchers(POST, "/v1/admin/actions/update_youtube_channel_names").hasRole(ROLE.UPDATE_VIDEOS)

            .antMatchers(DELETE, "/v1/videos/*").hasRole(ROLE.REMOVE_VIDEOS)
            .antMatchers(POST, "/v1/videos").hasRole(ROLE.INSERT_VIDEOS)
            .antMatchers(PATCH, "/v1/videos").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(POST, "/v1/videos/search").hasRole(ROLE.VIEW_DISABLED_VIDEOS)
            .antMatchers(POST, "/v1/videos/*").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(PATCH, "/v1/videos/*").hasRole(ROLE.RATE_VIDEOS)
            .antMatchers(GET, "/v1/videos*").hasRole(ROLE.VIEW_VIDEOS)
            .antMatchers(GET, "/v1/videos/*/transcript").hasRole(ROLE.DOWNLOAD_TRANSCRIPT)
            .antMatchers(GET, "/v1/videos/*").permitAll()

            .antMatchers(POST, "/v1/collections").hasRole(ROLE.INSERT_COLLECTIONS)
            .antMatchers(GET, "/v1/collections").hasRole(ROLE.VIEW_COLLECTIONS)
            .antMatchers(GET, "/v1/collections/*").hasRole(ROLE.VIEW_COLLECTIONS)
            .antMatchers(PATCH, "/v1/collections/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .antMatchers(DELETE, "/v1/collections/*").hasRole(ROLE.DELETE_COLLECTIONS)
            .antMatchers(PUT, "/v1/collections/*/videos/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .antMatchers(DELETE, "/v1/collections/*/videos/*").hasRole(ROLE.UPDATE_COLLECTIONS)

            .antMatchers(HEAD, "/v1/content-partners/*/videos/*").hasRole(ROLE.INSERT_VIDEOS)
            .antMatchers(POST, "/v1/content-partners").hasRole(ROLE.VIEW_CONTENT_PARTNERS)
            .antMatchers(GET, "/v1/content-partners").hasRole(ROLE.INSERT_CONTENT_PARTNERS)
            .antMatchers(PATCH, "/v1/content-partners/*").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)
            .antMatchers(PUT, "/v1/content-partners/*").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)
            .antMatchers(GET, "/v1/content-partners/*").hasRole(ROLE.VIEW_CONTENT_PARTNERS)

            .anyRequest().denyAll()
    }
}


