package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.video.VideoCaptionsCreated
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import mu.KLogging

class UpdateCaptions(val videoRepository: VideoRepository, private val playbackRepository: PlaybackRepository) {
    companion object : KLogging()

    @BoclipsEventListener
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
