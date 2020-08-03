package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.video.VideoTranscriptCreated
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoTranscriptNotFound
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class VideoTranscriptService(val videoRepository: VideoRepository) {
    companion object : KLogging()

    fun getTranscript(rawVideoId: String?): String {
        if (rawVideoId == null || rawVideoId.isBlank()) {
            throw VideoNotFoundException()
        }

        val videoId = VideoId(value = rawVideoId)
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)

        return video.voice.transcript ?: throw VideoTranscriptNotFound(videoId)
    }

    @BoclipsEventListener
    fun videoTranscriptReceived(videoTranscriptCreated: VideoTranscriptCreated) {
        try {
            val video = videoRepository.find(VideoId(value = videoTranscriptCreated.videoId))!!

            videoRepository.update(
                VideoUpdateCommand.ReplaceTranscript(
                    videoId = video.videoId,
                    transcript = videoTranscriptCreated.transcript
                )
            )

            logger.info { "Updated transcripts for ${video.videoId}" }
        } catch (ex: Exception) {
            logger.info { "Failed to update transcripts for ${videoTranscriptCreated.videoId}" }
        }
    }
}
