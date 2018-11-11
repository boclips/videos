package com.boclips.videos.service.infrastructure.playback

import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.model.playback.PlaybackProvider
import java.time.Duration

class TestYoutubePlaybackProvider : PlaybackProvider {
    private val playbackById = mutableMapOf<String, YoutubePlayback>()

    override fun retrievePlayback(videoIds: List<String>): Map<String, YoutubePlayback> {
        return videoIds
                .mapNotNull map@{ id ->
                    val youtubePlayback = playbackById[id] ?: return@map null
                    (id to youtubePlayback)
                }
                .toMap()
    }

    override fun removePlayback(videoId: String) {
    }

    fun addVideo(youtubeId: String, thumbnailUrl: String, duration: Duration): TestYoutubePlaybackProvider {
        playbackById[youtubeId] = YoutubePlayback(youtubeId, thumbnailUrl, duration)
        return this
    }

    fun clear() {
        playbackById.clear()
    }
}