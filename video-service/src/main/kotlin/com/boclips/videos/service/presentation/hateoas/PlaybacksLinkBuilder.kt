package com.boclips.videos.service.presentation.hateoas

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.Companion.hasManuallySetThumbnail
import com.boclips.videos.service.domain.model.playback.VideoPlayback.FaultyPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.VideoController
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class PlaybacksLinkBuilder(val kalturaClient: KalturaClient) {

    fun downloadLink(playback: VideoPlayback): HateoasLink? {
        return when (playback) {
            is StreamPlayback -> getIfHasRole(UserRoles.DOWNLOAD_VIDEO) {
                HateoasLink(
                    href = playback.downloadUrl,
                    rel = "download"
                )
            }
            else -> null
        }
    }

    fun thumbnailLink(playback: VideoPlayback): HateoasLink? {
        val href = when (playback) {
            is StreamPlayback -> getKalturaThumbnailUrl(playback)
            is YoutubePlayback -> playback.thumbnailUrl
            is FaultyPlayback -> null
        }

        return href?.let { HateoasLink(href = it, rel = "thumbnail") }
    }

    fun setThumbnail(playback: VideoPlayback, videoId: VideoId): HateoasLink? {
        return when (playback) {
            is StreamPlayback -> takeUnless { hasManuallySetThumbnail(playback) }?.let {
                getIfHasRole(UserRoles.UPDATE_VIDEOS) {
                    HateoasLink.of(
                        WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(VideoController::class.java).setThumbnailSecond(null, videoId.value)
                        ).withRel("setThumbnail")
                    )
                }
            }
            else -> null
        }
    }

    fun setCustomThumbnail(playback: VideoPlayback, videoId: VideoId): HateoasLink? {
        return when (playback) {
            is StreamPlayback -> takeUnless { hasManuallySetThumbnail(playback) }?.let {
                getIfHasRole(UserRoles.UPDATE_VIDEOS) {
                    HateoasLink.of(
                        WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(VideoController::class.java).setThumbnailImage(null, null, videoId.value)
                        ).withRel("setCustomThumbnail")
                    )
                }
            }
            else -> null
        }
    }

    fun deleteThumbnail(playback: VideoPlayback, videoId: VideoId): HateoasLink? {
        return when (playback) {
            is StreamPlayback -> takeIf { hasManuallySetThumbnail(playback) }?.let {
                getIfHasRole(UserRoles.UPDATE_VIDEOS) {
                    HateoasLink.of(
                        WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(VideoController::class.java).deleteManuallySetThumbnail(videoId.value)
                        ).withRel("deleteThumbnail")
                    )
                }
            }
            else -> null
        }
    }

    fun videoPreviewLink(playback: VideoPlayback): HateoasLink? {
        val href = when (playback) {
            is StreamPlayback -> kalturaClient.linkBuilder.getVideoPreviewUrl(playback.id.value)
            is YoutubePlayback -> null
            is FaultyPlayback -> null
        }

        return href?.let { HateoasLink(href = it, rel = "videoPreview") }
    }

    fun hlsStreamLink(playback: VideoPlayback): HateoasLink? {
        val href = when (playback) {
            is StreamPlayback -> kalturaClient.linkBuilder.getStreamUrl(playback.id.value, StreamFormat.APPLE_HDS)
            is YoutubePlayback -> null
            is FaultyPlayback -> null
        }

        return href?.let { HateoasLink(href = it, rel = "hlsStream") }
    }

    private fun getKalturaThumbnailUrl(playback: StreamPlayback): String = when  {
        playback.thumbnailSecond != null -> kalturaClient.linkBuilder.getThumbnailUrlBySecond(playback.id.value, playback.thumbnailSecond)
        playback.customThumbnail -> kalturaClient.linkBuilder.getThumbnailUrl(playback.id.value).substringBefore("/vid_slices")
        else -> kalturaClient.linkBuilder.getThumbnailUrl(playback.id.value)
    }
}
