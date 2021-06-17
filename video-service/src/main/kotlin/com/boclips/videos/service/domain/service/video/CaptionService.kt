package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.UnsupportedCaptionsException
import com.boclips.videos.service.domain.model.video.VideoId

class CaptionService(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository,
    private val captionValidator: CaptionValidator
) {

    fun getAvailableCaptions(videoId: VideoId): List<Caption> {
        return videoRepository.find(videoId)
            ?.let { video ->
                if (video.isBoclipsHosted()) {
                    playbackRepository.getCaptions(playbackId = video.playback.id)
                } else {
                    throw UnsupportedCaptionsException(video)
                }
            } ?: throw VideoNotFoundException(videoId)
    }

    fun getCaption(videoId: VideoId, humanGeneratedOnly: Boolean = false): Caption? {
        return videoRepository.find(videoId)?.let { video ->
            playbackRepository.getCaptions(playbackId = video.playback.id).firstOrNull {
                if (humanGeneratedOnly) {
                    it.isHumanGenerated
                } else {
                    true
                }
            }
        }
    }

    fun requestCaption(videoId: VideoId) {
        videoRepository.find(videoId)?.let { video ->
            (video.playback as? VideoPlayback.StreamPlayback)?.let {
                playbackRepository.requestCaptions(playbackId = video.playback.id)
            } ?: throw UnsupportedCaptionsException(video)
        } ?: throw VideoNotFoundException(videoId)
    }

    fun updateCaptionContent(videoId: VideoId, captionContent: String) {
        if (captionValidator.checkValid(captionContent)) {
            videoRepository.find(videoId)?.let { video ->
                playbackRepository.updateCaptionContent(video.playback.id, captionContent)

                videoRepository.update(
                    VideoUpdateCommand.ReplaceTranscript(
                        video.videoId, captionValidator.parse(captionContent).joinToString(separator = "\n"), video.voice.isTranscriptHumanGenerated
                    )
                )

                UpdateCaptions.logger.info { "Updated captions for ${video.videoId}" }
            }
        }
    }
}
