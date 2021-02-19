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
            .mvcMatchers(GET, "/actuator/health").permitAll()
            .mvcMatchers(GET, "/actuator/prometheus").permitAll()
            .mvcMatchers(GET, "/actuator/httptrace").permitAll()

            .antMatchers(OPTIONS, "/v1/**").permitAll()

            .mvcMatchers("/v1").permitAll()
            .mvcMatchers("/v1/").permitAll()

            .mvcMatchers(GET, "/v1/interactions").permitAll()
            .mvcMatchers(POST, "/v1/events/*").permitAll()
            .mvcMatchers(POST, "/v1/events/playback/batch").hasRole(ROLE.INSERT_EVENTS)

            .mvcMatchers(GET, "/v1/subjects").permitAll()
            .mvcMatchers(GET, "/v1/subjects/*").permitAll()
            .mvcMatchers(DELETE, "/v1/subjects/*").hasRole(ROLE.DELETE_SUBJECTS)
            .mvcMatchers(PUT, "/v1/subjects/*").hasRole(ROLE.UPDATE_SUBJECTS)
            .mvcMatchers(POST, "/v1/subjects").hasRole(ROLE.CREATE_SUBJECTS)

            .mvcMatchers(GET, "/v1/disciplines").hasRole(ROLE.VIEW_DISCIPLINES)
            .mvcMatchers(GET, "/v1/disciplines/*").hasRole(ROLE.VIEW_DISCIPLINES)
            .mvcMatchers(PUT, "/v1/disciplines/*").hasRole(ROLE.UPDATE_DISCIPLINES)
            .mvcMatchers(PUT, "/v1/disciplines/*/subjects").hasRole(ROLE.UPDATE_DISCIPLINES)
            .mvcMatchers(POST, "/v1/disciplines").hasRole(ROLE.INSERT_DISCIPLINES)

            .mvcMatchers(GET, "/v1/tags").hasRole(ROLE.VIEW_TAGS)
            .mvcMatchers(GET, "/v1/tags/*").hasRole(ROLE.VIEW_TAGS)
            .mvcMatchers(POST, "/v1/tags").hasRole(ROLE.INSERT_TAGS)
            .mvcMatchers(DELETE, "/v1/tags/*").hasRole(ROLE.DELETE_TAGS)

            .mvcMatchers(GET, "/v1/video-types").hasRole(ROLE.VIEW_VIDEO_TYPES)
            .mvcMatchers(GET, "/v1/attachment-types").hasRole(ROLE.VIEW_ATTACHMENT_TYPES)
            .mvcMatchers(GET, "/v1/content-categories").hasRole(ROLE.VIEW_CONTENT_CATEGORIES)

            .mvcMatchers(POST, "/v1/admin/actions/build_legacy_search_index").hasRole(ROLE.REBUILD_SEARCH_INDEX)
            .mvcMatchers(POST, "/v1/admin/actions/analyse_video/*").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(POST, "/v1/admin/actions/analyse_videos").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(POST, "/v1/admin/actions/clean_deactivated_videos").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(POST, "/v1/admin/actions/classify_videos").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(POST, "/v1/admin/actions/broadcast_videos").hasRole(ROLE.BROADCAST_EVENTS)
            .mvcMatchers(POST, "/v1/admin/actions/broadcast_collections").hasRole(ROLE.BROADCAST_EVENTS)
            .mvcMatchers(POST, "/v1/admin/actions/broadcast_channels").hasRole(ROLE.BROADCAST_EVENTS)
            .mvcMatchers(POST, "/v1/admin/actions/broadcast_contracts").hasRole(ROLE.BROADCAST_EVENTS)
            .mvcMatchers(POST, "/v1/admin/actions/broadcast_contract_legal_restrictions").hasRole(ROLE.BROADCAST_EVENTS)
            .mvcMatchers(POST, "/v1/admin/actions/update_youtube_channel_names").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(GET, "/v1/admin/actions/videos_for_content_package/*").hasRole(ROLE.VIEW_ADMIN_VIDEO_DATA)

            .mvcMatchers(GET, "/v1/content-warnings").hasRole(ROLE.VIEW_CONTENT_WARNINGS)
            .mvcMatchers(POST, "/v1/content-warnings").hasRole(ROLE.CREATE_CONTENT_WARNINGS)

            .mvcMatchers(DELETE, "/v1/videos/*").hasRole(ROLE.REMOVE_VIDEOS)
            .mvcMatchers(POST, "/v1/videos").hasRole(ROLE.INSERT_VIDEOS)
            .mvcMatchers(PATCH, "/v1/videos").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(POST, "/v1/videos/search").hasRole(ROLE.VIEW_DISABLED_VIDEOS)
            .mvcMatchers(POST, "/v1/videos/*").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(PATCH, "/v1/videos/*").hasAnyRole(ROLE.RATE_VIDEOS, ROLE.UPDATE_VIDEOS, ROLE.SHARE_VIDEOS)
            .mvcMatchers(GET, "/v1/videos").hasRole(ROLE.VIEW_VIDEOS)
            .mvcMatchers(GET, "/v1/videos/*/transcript").permitAll()
            .mvcMatchers(PUT, "/v1/videos/*/captions").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(GET, "/v1/videos/*/captions").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(PUT, "/v1/videos/*/attachments").hasRole(ROLE.UPDATE_VIDEOS)

            .mvcMatchers(DELETE, "/v1/videos/*/playback/thumbnail").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(PATCH, "/v1/videos/*/playback").hasRole(ROLE.UPDATE_VIDEOS)
            .mvcMatchers(POST, "/v1/videos/*/playback").hasRole(ROLE.UPDATE_VIDEOS)

            .mvcMatchers(GET, "/v1/videos/*/assets").hasRole(ROLE.DOWNLOAD_VIDEO)
            .mvcMatchers(PATCH, "/v1/videos/*/tags").hasRole(ROLE.TAG_VIDEOS)
            .mvcMatchers(POST, "/v1/videos/*/events").hasRole(ROLE.VIEW_VIDEOS)
            .mvcMatchers(GET, "/v1/videos/*/price").hasRole(ROLE.BOCLIPS_SERVICE)
            .mvcMatchers(GET, "/v1/videos/*/match").permitAll()
            .mvcMatchers(GET, "/v1/videos/*").permitAll()

            .mvcMatchers(GET, "/v1/users/*/collections").hasRole(ROLE.VIEW_COLLECTIONS)
            .mvcMatchers(POST, "/v1/collections").hasRole(ROLE.INSERT_COLLECTIONS)
            .mvcMatchers(GET, "/v1/collections").hasRole(ROLE.VIEW_COLLECTIONS)
            .mvcMatchers(GET, "/v1/collections/*").permitAll()
            .mvcMatchers(PATCH, "/v1/collections/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .mvcMatchers(DELETE, "/v1/collections/*").hasRole(ROLE.DELETE_COLLECTIONS)
            .mvcMatchers(PUT, "/v1/collections/*/videos/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .mvcMatchers(DELETE, "/v1/collections/*/videos/*").hasRole(ROLE.UPDATE_COLLECTIONS)
            .mvcMatchers(POST, "/v1/collections/*/events").hasRole(ROLE.VIEW_COLLECTIONS)

            .mvcMatchers(GET, "/v1/suggestions*").hasAnyRole(ROLE.VIEW_VIDEOS, ROLE.VIEW_COLLECTIONS)

            .mvcMatchers(HEAD, "/v1/content-partners/*/videos/*").hasRole(ROLE.INSERT_VIDEOS)
            .mvcMatchers(POST, "/v1/content-partners/*/videos/search").hasRole(ROLE.INSERT_VIDEOS)
            .mvcMatchers(POST, "/v1/content-partners").hasRole(ROLE.INSERT_CONTENT_PARTNERS)
            .mvcMatchers(GET, "/v1/content-partners").hasRole(ROLE.VIEW_CONTENT_PARTNERS)
            .mvcMatchers(POST, "/v1/content-partners/signed-upload-link").run {
                hasAnyRole(ROLE.INSERT_CONTENT_PARTNERS, ROLE.UPDATE_CONTENT_PARTNERS)
            }
            .mvcMatchers(PATCH, "/v1/content-partners/*").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)
            .mvcMatchers(PUT, "/v1/content-partners/*").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)
            .mvcMatchers(GET, "/v1/content-partners/*").hasRole(ROLE.VIEW_CONTENT_PARTNERS)
            .mvcMatchers(PUT, "/v1/content-partners/*/legal-restrictions").hasRole(ROLE.UPDATE_CONTENT_PARTNERS)

            .mvcMatchers(HEAD, "/v1/channels/*/videos/*").hasRole(ROLE.INSERT_VIDEOS)
            .mvcMatchers(POST, "/v1/channels/*/videos/search").hasRole(ROLE.INSERT_VIDEOS)
            .mvcMatchers(POST, "/v1/channels").hasRole(ROLE.INSERT_CHANNELS)
            .mvcMatchers(GET, "/v1/channels").hasRole(ROLE.VIEW_CHANNELS)
            .mvcMatchers(POST, "/v1/channels/signed-upload-link").run {
                hasAnyRole(ROLE.INSERT_CHANNELS, ROLE.UPDATE_CHANNELS)
            }
            .mvcMatchers(PATCH, "/v1/channels/*").hasRole(ROLE.UPDATE_CHANNELS)
            .mvcMatchers(PUT, "/v1/channels/*").hasRole(ROLE.UPDATE_CHANNELS)
            .mvcMatchers(GET, "/v1/channels/*").hasRole(ROLE.VIEW_CHANNELS)
            .mvcMatchers(PUT, "/v1/channels/*/legal-restrictions").hasRole(ROLE.UPDATE_CHANNELS)

            .mvcMatchers(GET, "/v1/contracts/*").hasRole(ROLE.VIEW_CONTRACTS)
            .mvcMatchers(GET, "/v1/contracts").hasRole(ROLE.VIEW_CONTRACTS)
            .mvcMatchers(POST, "/v1/contracts").hasRole(ROLE.INSERT_CONTRACTS)
            .mvcMatchers(POST, "/v1/contracts/signed-upload-link").run {
                hasRole(ROLE.INSERT_CONTRACTS)
            }
            .mvcMatchers(PATCH, "/v1/contracts/*").hasRole(ROLE.UPDATE_CONTRACTS)

            .mvcMatchers(GET, "/v1/distribution-methods").hasRole(ROLE.VIEW_DISTRIBUTION_METHODS)

            .mvcMatchers(GET, "/v1/legal-restrictions").hasRole(ROLE.VIEW_LEGAL_RESTRICTIONS)
            .mvcMatchers(GET, "/v1/legal-restrictions/*").hasRole(ROLE.VIEW_LEGAL_RESTRICTIONS)
            .mvcMatchers(POST, "/v1/legal-restrictions").hasRole(ROLE.CREATE_LEGAL_RESTRICTIONS)

            .mvcMatchers(GET, "/v1/age-ranges").hasRole(ROLE.VIEW_AGE_RANGES)
            .mvcMatchers(GET, "/v1/age-ranges/*").hasRole(ROLE.VIEW_AGE_RANGES)
            .mvcMatchers(POST, "/v1/age-ranges").hasRole(ROLE.INSERT_AGE_RANGES)

            .mvcMatchers(GET, "/v1/marketing-statuses").hasRole(ROLE.VIEW_MARKETING_STATUSES)

            .mvcMatchers(GET, "/v1/contract-legal-restrictions").hasRole(ROLE.VIEW_LEGAL_RESTRICTIONS)
            .mvcMatchers(POST, "/v1/contract-legal-restrictions").hasRole(ROLE.CREATE_LEGAL_RESTRICTIONS)

            .mvcMatchers(POST, "/v1/videos/metadata").hasRole(ROLE.VIEW_VIDEOS)

            .anyRequest().denyAll()
    }
}
