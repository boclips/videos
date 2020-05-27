package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.Video

class VideoDuplicationService(
    private val videoRepository: VideoRepository
) {
    fun markDuplicate(duplicatedVideo: Video, activeVideo: Video) {
        videoRepository.update(
            VideoUpdateCommand.MarkAsDuplicate(
                videoId = duplicatedVideo.videoId,
                activeVideoId = activeVideo.videoId
            )
        )
    }
}