package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource

class VideoToResourceConverter {
    fun convert(video: Video): VideoResource {
        return toResource(video)
    }

    fun convert(videos: List<Video>): List<VideoResource> {
        return videos.map(this::toResource)
    }

    private fun toResource(video: Video): VideoResource {
        return VideoResource(
                id = video.details.videoId.value,
                title = video.details.title,
                description = video.details.description,
                contentProvider = video.details.contentProvider,
                releasedOn = video.details.releasedOn,
                playback = getPlayback(video)
        )
    }

    private fun getPlayback(video: Video): PlaybackResource {
        val playback = video.playback
        val playbackResource = when (playback) {
            is StreamPlayback -> StreamPlaybackResource(type = "STREAM", streamUrl = playback.streamUrl)
            is YoutubePlayback -> YoutubePlaybackResource(type = "YOUTUBE", youtubeId = playback.youtubeId)
            else -> throw Exception()
        }
        playbackResource.thumbnailUrl = video.playback.thumbnailUrl
        playbackResource.duration = video.playback.duration
        return playbackResource
    }
}