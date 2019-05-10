package com.boclips.videos.service.application.video

import com.boclips.events.config.Topics
import com.boclips.events.types.VideoPlaybackSyncRequested
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import mu.KLogging
import org.springframework.messaging.support.MessageBuilder

open class RequestVideoPlaybackUpdate(
    private val videoAssetRepository: VideoAssetRepository,
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
                    AnalyseVideo.logger.info { "Video ${video.assetId} published to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
                }
            }
        } catch (ex: Exception) {
            logger.error { "Failed to publish (some) events to ${Topics.VIDEO_PLAYBACK_SYNC_REQUESTED}" }
        }
    }
}
