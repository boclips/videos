package com.boclips.videos.service.domain.service

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType

class PlaybackService(val kalturaPlaybackProvider: PlaybackProvider, val youtubePlaybackProvider: PlaybackProvider) {

    fun getVideosWithPlayback(videos: List<Video>): List<Video> {
        val kalturaPlaybackById = kalturaPlaybackProvider.retrievePlayback(videos.filter { video -> video.playbackId.playbackProviderType == PlaybackProviderType.KALTURA }.map { video -> video.playbackId.playbackId })
        val youtubePlaybackById = youtubePlaybackProvider.retrievePlayback(videos.filter { video -> video.playbackId.playbackProviderType == PlaybackProviderType.YOUTUBE }.map { video -> video.playbackId.playbackId })

        return videos.mapNotNull { video ->
            val videoPlayback = when(video.playbackId.playbackProviderType) {
                PlaybackProviderType.KALTURA -> kalturaPlaybackById[video.playbackId.playbackId]
                PlaybackProviderType.YOUTUBE -> youtubePlaybackById[video.playbackId.playbackId]
            }  ?: return@mapNotNull null

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