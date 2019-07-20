package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.video.VideoTranscriptCreated
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class UpdateTranscripts(val videoRepository: VideoRepository) {
    companion object : KLogging()

    @BoclipsEventListener
    operator fun invoke(videoTranscriptCreated: VideoTranscriptCreated) {
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
