package com.boclips.videos.service.presentation.hateoas

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.FaultyPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class PlaybacksLinkBuilder(val kalturaClient: KalturaClient) {

    fun downloadLink(playback: VideoPlayback): Link? {
        return when (playback) {
            is StreamPlayback -> getIfHasRole(UserRoles.DOWNLOAD_VIDEO) { Link(playback.downloadUrl, "download") }
            else -> null
        }
    }

    fun thumbnailLink(playback: VideoPlayback): Link? {
        val href = when (playback) {
            is StreamPlayback -> kalturaClient.linkBuilder.getThumbnailUrl(playback.id.value)
            is YoutubePlayback -> playback.thumbnailUrl
            is FaultyPlayback -> null
        }

        return href?.let { Link(it, "thumbnail") }
    }

    fun videoPreviewLink(playback: VideoPlayback): Link? {
        val href = when (playback) {
            is StreamPlayback -> kalturaClient.linkBuilder.getVideoPreviewUrl(playback.id.value)
            is YoutubePlayback -> null
            is FaultyPlayback -> null
        }

        return href?.let { Link(it, "videoPreview") }
    }

    fun hlsStreamLink(playback: VideoPlayback): Link? {
        val href = when (playback) {
            is StreamPlayback -> kalturaClient.linkBuilder.getStreamUrl(playback.id.value, StreamFormat.APPLE_HDS)
            is YoutubePlayback -> null
            is FaultyPlayback -> null
        }

        return href?.let { Link(it, "hlsStream") }
    }
}
