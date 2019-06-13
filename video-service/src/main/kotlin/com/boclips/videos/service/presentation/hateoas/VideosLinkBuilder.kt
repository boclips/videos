package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.VideoController
import com.boclips.videos.service.presentation.video.VideoResource
import currentUserHasRole
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class VideosLinkBuilder {

    fun self(videoResource: VideoResource): Link = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(VideoController::class.java)
            .getVideo(videoResource.id)
    ).withSelfRel()

    fun videoLink(): Link = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(VideoController::class.java)
            .getVideo(null)
    ).withRel("video")

    fun searchVideosLink() = getIfHasRole(UserRoles.VIEW_VIDEOS) {
        ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .search(null, null, null, null, null, null, null, null, null, null, null, null, null)
        ).withRel("searchVideos")
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

    fun transcriptLink(videoResource: VideoResource): Link? {
        if (!currentUserHasRole(UserRoles.DOWNLOAD_TRANSCRIPT)) {
            return null
        }

        if (false == videoResource.hasTranscripts) {
            return null
        }

        return ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .getTranscript(videoResource.id)
        ).withRel("transcript")
    }
}
