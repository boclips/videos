package com.boclips.videos.service.domain.service

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video

class PlaybackService(val kalturaPlaybackProvider: PlaybackProvider) {

    fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        val playbackById = kalturaPlaybackProvider.retrievePlayback(videos.map { video -> video.playbackId.playbackId })

        return videos.mapNotNull { video ->
            val videoPlayback = playbackById[video.playbackId.playbackId] ?: return@mapNotNull null

            video.copy(videoPlayback = videoPlayback)
        }
    }

    fun getVideoWithPlayback(video: Video): Video {
        val videosWithPlayback = getVideosWithPlayback(listOf(video))
        return videosWithPlayback.firstOrNull() ?: throw VideoPlaybackNotFound()
    }

    fun removePlayback(video: Video) {
        kalturaPlaybackProvider.removePlayback(video.playbackId.playbackId)
    }
}