package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoId

class CaptionService(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository,
    private val captionValidator: CaptionValidator
) {

    fun updateCaptionContent(videoId: VideoId, captionContent: String) {
        val captions = captionContent.replace("\\n", System.lineSeparator())
        if (captionValidator.checkValid(captionContent)) {
            videoRepository.find(videoId)?.let { video ->
                playbackRepository.updateCaptionContent(video.playback.id, captionContent)
                videoRepository.update(VideoUpdateCommand.ReplaceTranscript(
                    video.videoId, captionValidator.parse(captions).joinToString(separator = "\n")
                ))

                UpdateCaptions.logger.info { "Updated captions for ${video.videoId}" }
            }
        }
    }
}
