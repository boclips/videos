package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.config.Topics
import com.boclips.events.types.VideoPlaybackSyncRequested
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

open class RequestVideoPlaybackUpdate(
    private val videoAssetRepository: VideoAssetRepository,
    private val playbackRepository: PlaybackRepository,
    private val topics: Topics
) {
    companion object : KLogging()

    open operator fun invoke() {
        logger.info("Requesting video playback synchronization for all videos")

        try {
            videoAssetRepository.streamAll { sequence ->
                sequence.forEach { video ->
                    val videoToBeUpdated = VideoPlaybackSyncRequested.builder()
                        .videoId(video.assetId.value)
                        .build()

                    topics.videoPlaybackSyncRequested().send(MessageBuilder.withPayload(videoToBeUpdated).build())
                    logger.info { "Video ${video.assetId} published to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
                }
            }
        } catch (ex: Exception) {
            logger.error { "Failed to publish (some) events to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
        }
    }

    @StreamListener(Subscriptions.VIDEO_PLAYBACK_SYNC_REQUESTED)
    operator fun invoke(videoPlaybackSyncRequestedEvent: VideoPlaybackSyncRequested) {
        val potentialAssetToBeUpdated = AssetId(value = videoPlaybackSyncRequestedEvent.videoId)
        val actualAsset = videoAssetRepository.find(potentialAssetToBeUpdated)

        if (actualAsset == null) {
            logger.info { "Could find video $potentialAssetToBeUpdated" }
            return
        }

        val playback = playbackRepository.find(actualAsset.playbackId)
        if (playback == null) {
            logger.info { "Could not find playback information for $potentialAssetToBeUpdated" }
            return
        }

        val replacePlayback = VideoUpdateCommand.ReplacePlayback(
            assetId = actualAsset.assetId,
            playback = playback
        )

        try {
            videoAssetRepository.update(replacePlayback)
            logger.info { "Updated playback information for video ${actualAsset.assetId} successfully" }
        } catch (ex: Exception) {
            logger.info { "Did not update playback for video ${actualAsset.assetId}" }
        }
    }
}
