package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource

class VideoToResourceConverter {
    fun convert(video: Video): VideoResource {
        return toResource(video)
    }

    fun convert(videos: List<Video>): List<VideoResource> {
        return videos.map { video ->
            convert(video)
        }
    }

    private fun toResource(video: Video): VideoResource {
        val basicVideo = VideoResource(
                id = video.videoId.videoId,
                title = video.title,
                description = video.description,
                contentProvider = video.contentProvider,
                releasedOn = video.releasedOn)

        if (video.isPlayable()) {
            val playback = video.videoPlayback!!

            val playbackResource = when (playback) {
                is StreamPlayback -> StreamPlaybackResource(streamUrl = playback.streamUrl)
                is YoutubePlayback -> YoutubePlaybackResource(youtubeId = playback.youtubeId)
                else -> throw Exception()
            }
            playbackResource.thumbnailUrl = video.videoPlayback.thumbnailUrl
            playbackResource.duration = video.videoPlayback.duration

            return basicVideo.copy(playback = playbackResource)
        }

        return basicVideo
    }
}