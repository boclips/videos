package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.security.utils.UserExtractor.getIfHasAnyRole
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.presentation.EventController
import com.boclips.videos.service.presentation.VideoController
import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class VideosLinkBuilder {

    object Rels {
        const val VIDEO = "video"
        const val CREATE_VIDEO_INTERACTED_WITH_EVENT = "createVideoInteractedWithEvent"
        const val SEARCH_VIDEOS = "searchVideos"
        const val ADMIN_SEARCH = "adminSearch"
        const val VIDEOS = "videos"
        const val TRANSCRIPT = "transcript"
        const val RATE = "rate"
        const val TAG = "tag"
        const val UPDATE = "update"
    }

    fun self(videoResource: VideoResource): Link = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(VideoController::class.java)
            .getVideo(videoResource.id)
    ).withSelfRel()

    fun videoLink(): Link = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(VideoController::class.java)
            .getVideo(null)
    ).withRel(Rels.VIDEO)

    fun createVideoInteractedWithEvent(videoResource: VideoResource): Link = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java)
            .logVideoInteractedWithEvent(videoId = videoResource.id!!, videoInteractedWith = true, type = null)
    ).withRel(Rels.CREATE_VIDEO_INTERACTED_WITH_EVENT)

    fun searchVideosLink() = when {
        currentUserHasRole(UserRoles.VIEW_VIDEOS) -> ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .search(null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        ).withRel(Rels.SEARCH_VIDEOS)

        else -> null
    }

    fun videosLink() =
        getIfHasAnyRole(UserRoles.UPDATE_VIDEOS, UserRoles.INSERT_VIDEOS) {
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(VideoController::class.java)
                    .patchMultipleVideos(null)
            ).withRel(Rels.VIDEOS)
        }

    fun adminSearchLink() = getIfHasRole(UserRoles.VIEW_DISABLED_VIDEOS) {
        ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .adminSearch(null)
        ).withRel(Rels.ADMIN_SEARCH)
    }

    fun transcriptLink(videoResource: VideoResource) = when {

        !currentUserHasRole(UserRoles.DOWNLOAD_TRANSCRIPT) -> null
        videoResource.hasTranscripts == false -> null

        else -> ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .getTranscript(videoResource.id)
        ).withRel(Rels.TRANSCRIPT)
    }

    fun rateLink(video: Video) = getIfHasRole(UserRoles.RATE_VIDEOS) {
        ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .patchRating(null, video.videoId.value)
        ).withRel(Rels.RATE)
    }

    fun tagLink(video: Video) = when {

        !currentUserHasRole(UserRoles.TAG_VIDEOS) -> null
        video.tag != null -> null

        else -> ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .patchTag(video.videoId.value, null)
        ).withRel(Rels.TAG)
    }

    fun updateLink(video: Video) = getIfHasRole(UserRoles.UPDATE_VIDEOS) {
        ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(VideoController::class.java)
                .patchVideo(id = video.videoId.value, title = null, description = null)
        ).withRel(Rels.UPDATE)
    }
}
