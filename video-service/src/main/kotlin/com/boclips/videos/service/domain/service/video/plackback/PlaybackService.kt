package com.boclips.videos.service.domain.service.video.plackback

import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

open class PlaybackService(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging()

    open fun updatePlaybackFor(filter: VideoFilter) {
        videoRepository.streamAll(filter) { sequence ->
            sequence.forEach { video ->
                updatePlaybackFor(video)
            }
        }
    }

    private fun updatePlaybackFor(video: Video) {
        if (!video.isPlayable()) {
            logger.info { "Video ${video.videoId} has no playback information associated with it." }
            return
        }

        val playback = playbackRepository.find(video.playback.id)
        if (playback == null) {
            logger.info { "Could not find playback information for ${video.videoId} (Playback Id: ${video.playback.id})" }
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
