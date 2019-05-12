package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.config.Topics
import com.boclips.events.types.VideoPlaybackSyncRequested
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

open class RequestVideoPlaybackUpdate(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository,
    private val topics: Topics
) {
    companion object : KLogging()

    open operator fun invoke() {
        logger.info("Requesting video playback synchronization for all videos")

        try {
            videoRepository.streamAll { sequence ->
                sequence.forEach { video ->
                    val videoToBeUpdated = VideoPlaybackSyncRequested.builder()
                        .videoId(video.videoId.value)
                        .build()

                    topics.videoPlaybackSyncRequested().send(MessageBuilder.withPayload(videoToBeUpdated).build())
                    logger.info { "Video ${video.videoId} published to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
                }
            }
        } catch (ex: Exception) {
            logger.error { "Failed to publish (some) events to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
        }
    }

    @StreamListener(Subscriptions.VIDEO_PLAYBACK_SYNC_REQUESTED)
    operator fun invoke(videoPlaybackSyncRequestedEvent: VideoPlaybackSyncRequested) {
        try {
            handleUpdate(videoPlaybackSyncRequestedEvent)
        } catch (ex: Exception) {
            logger.info { "Failed to process ${Subscriptions.VIDEO_PLAYBACK_SYNC_REQUESTED} for ${videoPlaybackSyncRequestedEvent.videoId}" }
        }
    }

    private fun handleUpdate(videoPlaybackSyncRequestedEvent: VideoPlaybackSyncRequested) {
        val potentialVideoToBeUpdated = VideoId(value = videoPlaybackSyncRequestedEvent.videoId)
        val actualVideo = videoRepository.find(potentialVideoToBeUpdated)

        if (actualVideo == null) {
            logger.info { "Could find video $potentialVideoToBeUpdated" }
            return
        }

        if (!actualVideo.isPlayable()) {
            logger.info { "Video $potentialVideoToBeUpdated has no playback information associated with it." }
            return
        }

        val playback = playbackRepository.find(actualVideo.playback.id)
        if (playback == null) {
            logger.info { "Could not find playback information for $potentialVideoToBeUpdated" }
            return
        }

        val replacePlayback = VideoUpdateCommand.ReplacePlayback(
            videoId = actualVideo.videoId,
            playback = playback
        )

        try {
            videoRepository.update(replacePlayback)
            logger.info { "Updated playback information for video ${actualVideo.videoId} successfully" }
        } catch (ex: Exception) {
            logger.info { "Did not update playback for video ${actualVideo.videoId}" }
        }
    }
}
