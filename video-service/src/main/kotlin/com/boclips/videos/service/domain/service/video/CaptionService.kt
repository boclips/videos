package com.boclips.videos.service.domain.service.video

import com.boclips.videos.api.response.video.CaptionsResource
import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.VideoId

class CaptionService(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository,
    private val captionValidator: CaptionValidator
) {

    fun getAvailableCaptions(videoId: VideoId): List<Caption> {
        return videoRepository.find(videoId)?.let { video ->
            playbackRepository.getCaptions(playbackId = video.playback.id)
        } ?: throw VideoNotFoundException(videoId)
    }

    fun getCaptionContent(videoId: VideoId): String? {
        return videoRepository.find(videoId)?.let { video ->
            playbackRepository.getCaptions(playbackId = video.playback.id).firstOrNull()?.content
        }
    }

    fun updateCaptionContent(videoId: VideoId, captionContent: String) {
        if (captionValidator.checkValid(captionContent)) {
            videoRepository.find(videoId)?.let { video ->
                playbackRepository.updateCaptionContent(video.playback.id, captionContent)

                videoRepository.update(VideoUpdateCommand.ReplaceTranscript(
                    video.videoId, captionValidator.parse(captionContent).joinToString(separator = "\n")
                ))

                UpdateCaptions.logger.info { "Updated captions for ${video.videoId}" }
            }
        }
    }
}
