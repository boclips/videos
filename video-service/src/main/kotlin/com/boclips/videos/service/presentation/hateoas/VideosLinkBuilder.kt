package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.application.currentUserHasRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.presentation.VideoController
import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class VideosLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun self(videoResource: VideoResource): Link = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(VideoController::class.java)
            .getVideo(videoResource.id)
    ).withSelfRel()

    fun videoLink(): Link = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(VideoController::class.java)
            .getVideo(null)
    ).withRel("video")

    fun searchVideosLink() = when {
        currentUserHasRole(UserRoles.VIEW_ANY_VIDEO) -> Link(
            getVideosRoot()
                .toUriString() + "{?query,sort_by,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,size,page,subject}"
        ).withRel("searchVideos")

        currentUserHasRole(UserRoles.VIEW_VIDEOS) -> ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .search(null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        ).withRel("searchVideos")

        else -> null
    }

    fun videosLink() =
        getIfHasAnyRole(UserRoles.UPDATE_VIDEOS, UserRoles.INSERT_VIDEOS) {
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(VideoController::class.java)
                    .patchMultipleVideos(null)
            ).withRel("videos")
        }

    fun adminSearchLink() = getIfHasRole(UserRoles.VIEW_DISABLED_VIDEOS) {
        ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .adminSearch(null)
        ).withRel("adminSearch")
    }

    fun transcriptLink(videoResource: VideoResource) = when {

        !currentUserHasRole(UserRoles.DOWNLOAD_TRANSCRIPT) -> null
        videoResource.hasTranscripts == false -> null

        else -> ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .getTranscript(videoResource.id)
        ).withRel("transcript")
    }

    fun rateLink(video: Video) = getIfHasRole(UserRoles.RATE_VIDEOS) {
         ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .patchRating(null, video.videoId.value)
        ).withRel("rate")
    }

    fun tagLink(video: Video) = when {

        !currentUserHasRole(UserRoles.TAG_VIDEOS) -> null
        video.tag != null -> null

        else -> ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .patchTag(video.videoId.value, null)
        ).withRel("tag")
    }

    private fun getVideosRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/videos")
        .replaceQueryParams(null)
}
