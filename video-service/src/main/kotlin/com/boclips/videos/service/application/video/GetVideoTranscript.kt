package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoTranscriptNotFound
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository

class GetVideoTranscript(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(id: String?): String {
        if (id == null || id.isBlank()) {
            throw VideoNotFoundException()
        }

        val videoId = VideoId(value = id)
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)

        if (video.transcript == null) {
            throw VideoTranscriptNotFound(videoId)
        }

        return video.transcript
    }
}