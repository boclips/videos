package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.security.utils.UserExtractor.getIfAuthenticated
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.presentation.VideoController
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.LOG_VIDEO_INTERACTION
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.SEARCH_VIDEOS
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.UPDATE
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.VIDEO
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class VideosLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val VIDEO = "video"
        const val LOG_VIDEO_INTERACTION = "logInteraction"
        const val SEARCH_VIDEOS = "searchVideos"
        const val ADMIN_SEARCH = "adminSearch"
        const val ADMIN_VIDEO_SEARCH = "adminVideoSearch"
        const val TRANSCRIPT = "transcript"
        const val RATE = "rate"
        const val TAG = "tag"
        const val UPDATE = "update"
    }

    fun self(videoId: String?): HateoasLink {
        return HateoasLink.of(Link(getVideosRoot().pathSegment(videoId).build().toUriString(), "self"))
    }

    fun videoLink(): HateoasLink {
        return HateoasLink.of(Link(getVideosRoot().pathSegment("{id}").build().toUriString(), VIDEO))
    }

    fun createVideoInteractedWithEvent(videoId: String?): HateoasLink {
        return HateoasLink.of(
            Link(
                getVideosRoot()
                    .pathSegment("$videoId")
                    .pathSegment("events")
                    .queryParam("logVideoInteraction", true)
                    .queryParam("type", "{type}")
                    .build()
                    .toUriString(), LOG_VIDEO_INTERACTION
            )
        )
    }

    fun searchVideosLink(): HateoasLink? {
        return when {
            currentUserHasRole(UserRoles.VIEW_VIDEOS) -> {
                HateoasLink.of(
                    Link(
                        getVideosRoot()
                            .build()
                            .toUriString()
                            + "{?query,sort_by,duration,duration_facets,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,age_range,age_range_facets,size,page,subject,subjects_set_manually,promoted,content_partner,type}",
                        SEARCH_VIDEOS
                    )
                )
            }
            else -> null
        }
    }

    fun adminSearchLink(): HateoasLink? {
        return getIfHasRole(UserRoles.VIEW_DISABLED_VIDEOS) {
            HateoasLink.of(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(VideoController::class.java)
                        .getVideosAdmin(null)
                ).withRel(Rels.ADMIN_SEARCH)
            )
        }
    }

    fun adminVideoSearchLink(): HateoasLink? {
        return getIfHasRole(UserRoles.VIEW_DISABLED_VIDEOS) {
            HateoasLink.of(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(VideoController::class.java)
                        .getVideosAdmin(null)
                ).withRel(Rels.ADMIN_VIDEO_SEARCH)
            )
        }
    }

    fun transcriptLink(video: Video): HateoasLink? {
        return when {
            !currentUserHasRole(UserRoles.DOWNLOAD_TRANSCRIPT) -> null
            !video.hasTranscript() -> null

            else -> HateoasLink.of(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(VideoController::class.java)
                        .getTranscript(video.videoId.value)
                ).withRel(Rels.TRANSCRIPT)
            )
        }
    }

    fun rateLink(video: Video): HateoasLink? =
        getIfAuthenticated {
            getIfHasRole(UserRoles.RATE_VIDEOS) {
                HateoasLink.of(
                    Link(
                        getVideosRoot()
                            .pathSegment(video.videoId.value)
                            .queryParam("rating", "{rating}")
                            .build()
                            .toUriString()
                    ).withRel(Rels.RATE)
                )
            }
        }

    fun tagLink(video: Video): HateoasLink? {
        return when {
            !currentUserHasRole(UserRoles.TAG_VIDEOS) -> null
            video.tags.isNotEmpty() -> null

            else -> HateoasLink.of(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(VideoController::class.java)
                        .patchTag(video.videoId.value, null)
                ).withRel(Rels.TAG)
            )
        }
    }

    fun updateLink(video: Video): HateoasLink? = getIfHasRole(UserRoles.UPDATE_VIDEOS) {
        getIfHasRole(UserRoles.UPDATE_VIDEOS) {
            HateoasLink.of(
                Link(
                    getVideosRoot()
                        .pathSegment(video.videoId.value)
                        .build()
                        .toUriString()
                        + "{?title,description,promoted,subjectIds,ageRangeIds}"
                    , UPDATE
                )
            )
        }
    }

    private fun getVideosRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/videos")
        .replaceQueryParams(null)
}
