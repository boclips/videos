package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.video.VideoCaptionsCreated
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class UpdateCaptions(val videoRepository: VideoRepository, private val playbackRepository: PlaybackRepository) {
    companion object : KLogging()

    @StreamListener(Subscriptions.VIDEO_CAPTIONS_CREATED)
    operator fun invoke(videoCaptionsCreated: VideoCaptionsCreated) {
        try {
            val video = videoRepository.find(VideoId(value = videoCaptionsCreated.videoId))!!

            playbackRepository.uploadCaptions(video.playback.id, videoCaptionsCreated.captions)

            logger.info { "Updated captions for ${video.videoId}" }
        } catch (ex: Exception) {
            logger.info { "Failed to update captions for ${videoCaptionsCreated.videoId}" }
        }
    }
}