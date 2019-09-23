package com.boclips.videos.service.presentation.hateoas

import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.FaultyPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class PlaybacksLinkBuilder(val kalturaClient: KalturaClient) {

    fun createThumbnailUrl(playback: VideoPlayback): Link? {
        val href = when (playback) {
            is StreamPlayback -> kalturaClient.getThumbnailUrl(playback.id.value)
            is YoutubePlayback -> playback.thumbnailUrl
            is FaultyPlayback -> null
        }

        return href?.let { Link(it, "thumbnail") }
    }

    fun createVideoPreviewUrl(playback: VideoPlayback): Link? {
        val href = when (playback) {
            is StreamPlayback -> kalturaClient.getVideoPreviewUrl(playback.id.value)
            is YoutubePlayback -> null
            is FaultyPlayback -> null
        }

        return href?.let { Link(it, "videoPreview") }
    }

}