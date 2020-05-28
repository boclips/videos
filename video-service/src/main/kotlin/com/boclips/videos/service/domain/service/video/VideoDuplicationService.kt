package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.VideoId

class VideoDuplicationService(
    private val videoRepository: VideoRepository
) {
    fun markDuplicate(
        videoId: VideoId,
        activeVideoId: VideoId
    ) {
        videoRepository.update(
            VideoUpdateCommand.MarkAsDuplicate(
                videoId = videoId,
                activeVideoId = activeVideoId
            )
        )
    }
}