package com.boclips.videos.service.infrastructure.playback

import com.boclips.events.types.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import java.time.Duration

class TestYoutubePlaybackProvider : PlaybackProvider {
    private val playbackById = mutableMapOf<PlaybackId, YoutubePlayback>()

    override fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, YoutubePlayback> {
        return playbackIds
            .mapNotNull map@{ id ->
                val youtubePlayback = playbackById[id] ?: return@map null
                (id to youtubePlayback)
            }
            .toMap()
    }

    override fun removePlayback(playbackId: PlaybackId) {
    }

    override fun uploadCaptions(playbackId: PlaybackId, captions: Captions) {
        throw UnsupportedOperationException("Uploading captions to YouTube is not supported")
    }

    fun addVideo(youtubeId: String, thumbnailUrl: String, duration: Duration): TestYoutubePlaybackProvider {
        val playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = youtubeId)

        playbackById[playbackId] = YoutubePlayback(
            id = playbackId,
            thumbnailUrl = thumbnailUrl,
            duration = duration
        )

        return this
    }

    fun clear() {
        playbackById.clear()
    }
}
