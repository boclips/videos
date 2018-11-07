package com.boclips.videos.service.infrastructure.playback

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import java.time.Duration

class TestYoutubePlaybackProvider : PlaybackProvider {

    private val playbackById = mutableMapOf<String, YoutubePlayback>()

    override fun removePlayback(video: Video) {
    }

    override fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        return videos.map { video -> video.copy(videoPlayback = playbackById[video.playbackId.playbackId]) }
    }

    fun addVideo(youtubeId: String, thumbnailUrl: String, duration: Duration): TestYoutubePlaybackProvider {
        playbackById[youtubeId] = YoutubePlayback(youtubeId, thumbnailUrl, duration)
        return this
    }
}