package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class UpdatePlayback(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository
) {

    companion object : KLogging()

    operator fun invoke(videoId: VideoId) {
        handleUpdate(videoId)
    }

    @BoclipsEventListener()
    operator fun invoke(videoPlaybackSyncRequestedEvent: VideoPlaybackSyncRequested) {
        try {
            handleUpdate(VideoId(value = videoPlaybackSyncRequestedEvent.videoId))
        } catch (ex: Exception) {
            logger.info { "Failed to process playback synchronization request for video ${videoPlaybackSyncRequestedEvent.videoId}: $ex" }
        }
    }

    private fun handleUpdate(videoId: VideoId) {
        val video = videoRepository.find(videoId)

        if (video == null) {
            logger.info { "Could find video $videoId" }
            return
        }

        if (!video.isPlayable()) {
            logger.info { "Video $videoId has no playback information associated with it." }
            return
        }

        val playback = playbackRepository.find(video.playback.id)
        if (playback == null) {
            logger.info { "Could not find playback information for $videoId (Playback Id: ${video.playback.id})" }
            return
        }

        val replacePlayback = VideoUpdateCommand.ReplacePlayback(
            videoId = video.videoId,
            playback = playback
        )

        try {
            videoRepository.update(replacePlayback)
            logger.info { "Updated playback information for video ${video.videoId} successfully" }
        } catch (ex: Exception) {
            logger.info { "Did not update playback for video ${video.videoId}: $ex" }
        }
    }
}
