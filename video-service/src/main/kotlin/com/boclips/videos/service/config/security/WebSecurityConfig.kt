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
            .antMatchers(POST, "/v1/events/playback/batch").hasRole(ROLE.INSERT_EVENTS)

            .antMatchers(GET, "/v1/subjects").permitAll()
            .antMatchers(GET, "/v1/subjects/*").permitAll()
            .antMatchers(DELETE, "/v1/subjects/*").hasRole(ROLE.DELETE_SUBJECTS)
            .antMatchers(PUT, "/v1/subjects/*").hasRole(ROLE.UPDATE_SUBJECTS)
            .antMatchers(POST, "/v1/subjects").hasRole(ROLE.CREATE_SUBJECTS)

            .antMatchers(GET, "/v1/disciplines").hasRole(ROLE.VIEW_DISCIPLINES)
            .antMatchers(GET, "/v1/disciplines/*").hasRole(ROLE.VIEW_DISCIPLINES)
            .antMatchers(PUT, "/v1/disciplines/*").hasRole(ROLE.UPDATE_DISCIPLINES)
            .antMatchers(PUT, "/v1/disciplines/*/subjects").hasRole(ROLE.UPDATE_DISCIPLINES)
            .antMatchers(POST, "/v1/disciplines").hasRole(ROLE.INSERT_DISCIPLINES)

            .antMatchers(GET, "/v1/tags").hasRole(ROLE.VIEW_TAGS)
            .antMatchers(GET, "/v1/tags/*").hasRole(ROLE.VIEW_TAGS)
            .antMatchers(POST, "/v1/tags").hasRole(ROLE.INSERT_TAGS)
            .antMatchers(DELETE, "/v1/tags/*").hasRole(ROLE.DELETE_TAGS)

            .antMatchers(GET, "/v1/video-types").hasRole(ROLE.VIEW_VIDEO_TYPES)
            .antMatchers(GET, "/v1/content-categories").hasRole(ROLE.VIEW_CONTENT_CATEGORIES)

            .antMatchers(POST, "/v1/admin/actions/build_legacy_search_index").hasRole(ROLE.REBUILD_SEARCH_INDEX)
            .antMatchers(POST, "/v1/admin/actions/analyse_video/*").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(POST, "/v1/admin/actions/analyse_videos").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(POST, "/v1/admin/actions/classify_videos").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(POST, "/v1/admin/actions/broadcast_videos").hasRole(ROLE.UPDATE_VIDEOS)

            .antMatchers(POST, "/v1/admin/actions/update_youtube_channel_names").hasRole(ROLE.UPDATE_VIDEOS)

            .antMatchers(DELETE, "/v1/videos/*").hasRole(ROLE.REMOVE_VIDEOS)
            .antMatchers(POST, "/v1/videos").hasRole(ROLE.INSERT_VIDEOS)
            .antMatchers(PATCH, "/v1/videos").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(POST, "/v1/videos/search").hasRole(ROLE.VIEW_DISABLED_VIDEOS)
            .antMatchers(POST, "/v1/videos/*").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(PATCH, "/v1/videos/*").hasAnyRole(ROLE.RATE_VIDEOS, ROLE.UPDATE_VIDEOS, ROLE.SHARE_VIDEOS)
            .antMatchers(GET, "/v1/videos").hasRole(ROLE.VIEW_VIDEOS)
            .antMatchers(GET, "/v1/videos/*/transcript").hasRole(ROLE.DOWNLOAD_TRANSCRIPT)
            .antMatchers(PUT, "/v1/videos/*/captions").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(GET, "/v1/videos/*/captions").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(PUT, "/v1/videos/*/attachments").hasRole(ROLE.UPDATE_VIDEOS)

            .antMatchers(DELETE, "/v1/videos/*/playback/thumbnail").hasRole(ROLE.UPDATE_VIDEOS)
            .antMatchers(PATCH, "/v1/videos/*/playback").hasRole(ROLE.UPDATE_VIDEOS)

            .antMatchers(GET, "/v1/videos/*/assets").hasRole(ROLE.DOWNLOAD_VIDEO)
            .antMatchers(PATCH, "/v1/videos/*/tags").hasRole(ROLE.TAG_VIDEOS)
            .antMatchers(POST, "/v1/videos/*/events").hasRole(ROLE.VIEW_VIDEOS)
            .antMatchers(GET, "/v1/videos/*/match").permitAll()
            .antMatchers(GET, "/v1/videos/*").permitAll()

            .antMatchers(POST, "/v1/collections").hasRole(ROLE.INSERT_COLLECTIONS)
            .antMatchers(GET, "/v1/collections").hasRole(ROLE.VIEW_COLLECTIONS)
            .antMatchers(GET, "/v1/collections/*").permitAll()
            .antMatchers(PATCH, "/v1/collections/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .antMatchers(DELETE, "/v1/collections/*").hasRole(ROLE.DELETE_COLLECTIONS)
            .antMatchers(PUT, "/v1/collections/*/videos/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .antMatchers(DELETE, "/v1/collections/*/videos/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .antMatchers(POST, "/v1/collections/*/events").hasRole(ROLE.VIEW_COLLECTIONS)

            .antMatchers(GET, "/v1/suggestions*").hasAnyRole(ROLE.VIEW_VIDEOS, ROLE.VIEW_COLLECTIONS)

            .antMatchers(HEAD, "/v1/content-partners/*/videos/*").hasRole(ROLE.INSERT_VIDEOS)
            .antMatchers(POST, "/v1/content-partners/*/videos/search").hasRole(ROLE.INSERT_VIDEOS)
            .antMatchers(POST, "/v1/content-partners").hasRole(ROLE.INSERT_CONTENT_PARTNERS)
            .antMatchers(GET, "/v1/content-partners").hasRole(ROLE.VIEW_CONTENT_PARTNERS)
            .antMatchers(POST, "/v1/content-partners/signed-upload-link").run {
                hasAnyRole(ROLE.INSERT_CONTENT_PARTNERS, ROLE.UPDATE_CONTENT_PARTNERS)
            }
            .antMatchers(PATCH, "/v1/content-partners/*").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)
            .antMatchers(PUT, "/v1/content-partners/*").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)
            .antMatchers(GET, "/v1/content-partners/*").hasRole(ROLE.VIEW_CONTENT_PARTNERS)
            .antMatchers(PUT, "/v1/content-partners/*/legal-restrictions").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)

            .antMatchers(GET, "/v1/content-partner-contracts/*").hasRole(ROLE.VIEW_CONTENT_PARTNER_CONTRACTS)
            .antMatchers(GET, "/v1/content-partner-contracts").hasRole(ROLE.VIEW_CONTENT_PARTNER_CONTRACTS)
            .antMatchers(POST, "/v1/content-partner-contracts").hasRole(ROLE.INSERT_CONTENT_PARTNER_CONTRACTS)
            .antMatchers(POST, "/v1/content-partner-contracts/signed-upload-link").run {
                hasRole(ROLE.INSERT_CONTENT_PARTNER_CONTRACTS)
            }
            .antMatchers(PATCH, "/v1/content-partner-contracts/*").hasRole(ROLE.UPDATE_CONTENT_PARTNER_CONTRACTS)

            .antMatchers(GET, "/v1/distribution-methods").hasRole(ROLE.VIEW_DISTRIBUTION_METHODS)

            .antMatchers(GET, "/v1/legal-restrictions").hasRole(ROLE.VIEW_LEGAL_RESTRICTIONS)
            .antMatchers(GET, "/v1/legal-restrictions/*").hasRole(ROLE.VIEW_LEGAL_RESTRICTIONS)
            .antMatchers(POST, "/v1/legal-restrictions").hasRole(ROLE.CREATE_LEGAL_RESTRICTIONS)

            .antMatchers(GET, "/v1/age-ranges").hasRole(ROLE.VIEW_AGE_RANGES)
            .antMatchers(GET, "/v1/age-ranges/*").hasRole(ROLE.VIEW_AGE_RANGES)
            .antMatchers(POST, "/v1/age-ranges").hasRole(ROLE.INSERT_AGE_RANGES)

            .antMatchers(GET, "/v1/marketing-statuses").hasRole(ROLE.VIEW_MARKETING_STATUSES)

            .antMatchers(GET, "/v1/contract-legal-restrictions").hasRole(ROLE.VIEW_LEGAL_RESTRICTIONS)
            .antMatchers(POST, "/v1/contract-legal-restrictions").hasRole(ROLE.CREATE_LEGAL_RESTRICTIONS)

            .anyRequest().denyAll()
    }
}


