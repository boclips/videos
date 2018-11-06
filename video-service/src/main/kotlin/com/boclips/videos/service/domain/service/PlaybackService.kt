package com.boclips.videos.service.domain.service

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video

class PlaybackService(val kalturaPlaybackProvider: PlaybackProvider) {

    fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        return kalturaPlaybackProvider.getVideosWithPlayback(videos)
    }

    fun removePlayback(video: Video) {
        kalturaPlaybackProvider.removePlayback(video)
    }

    fun getVideoWithPlayback(video: Video): Video {
        val videosWithPlayback = getVideosWithPlayback(listOf(video))
        return videosWithPlayback.firstOrNull() ?: throw VideoPlaybackNotFound()
    }
}