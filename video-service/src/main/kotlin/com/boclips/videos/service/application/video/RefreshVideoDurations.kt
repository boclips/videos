package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.videos.service.domain.model.asset.PartialVideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RefreshVideoDurations(
    private val videoAssetRepository: VideoAssetRepository,
    private val playbackRepository: PlaybackRespository
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Starting a refresh of video durations")

        videoAssetRepository.streamAllSearchable { videos ->
            videos.forEach { updateVideoDuration(it) }
        }

        logger.info("Completed refresh of video durations")
        return CompletableFuture.completedFuture(null)
    }

    private fun updateVideoDuration(video: VideoAsset) {
        playbackRepository.find(video.playbackId)?.let {
            if (it.duration != video.duration) {
                logger.info("Updating duration video=${video.assetId.value} before=${video.duration} after=${it.duration}")
                videoAssetRepository.update(video.assetId, PartialVideoAsset(duration = it.duration))
            }
        }
    }
}