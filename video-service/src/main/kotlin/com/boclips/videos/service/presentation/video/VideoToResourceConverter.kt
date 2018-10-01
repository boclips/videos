package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.Video

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
                releasedOn = video.releasedOn,
                duration = video.duration)

        if (video.isPlayable()) {
            return basicVideo.copy(
                    streamUrl = video.videoPlayback!!.streamUrl,
                    thumbnailUrl = video.videoPlayback!!.thumbnailUrl
            )
        }

        return basicVideo
    }
}