package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.PartialVideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RefreshVideoDurations(
    private val videoAssetRepository: VideoAssetRepository,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Starting a refresh of video durations")
        val future = CompletableFuture<Unit>()

        try {
            refreshDurations(notifier)

            logger.info("Completed refresh of video durations")
            future.complete(null)
        } catch (e: Exception) {
            logger.error("Error refreshing video durations", e)
            future.completeExceptionally(e)
        }

        return future
    }

    private fun refreshDurations(notifier: ProgressNotifier?) {
        videoAssetRepository.streamAllSearchable { sequence ->
            var batch = 0

            sequence.chunked(size = 50).forEach { videos ->
                notifier?.send("Processing durations updates batch ${batch++}")

                val updatesByAssetId = durationsToUpdate(videos)
                if (updatesByAssetId.isNotEmpty()) {
                    logger.info("Updating durations for ${updatesByAssetId.size} videos")
                    notifier?.send("Updating durations for ${updatesByAssetId.size} videos")
                    videoAssetRepository.bulkUpdate(updatesByAssetId)
                }
            }
        }
    }

    private fun durationsToUpdate(videos: List<VideoAsset>): List<Pair<AssetId, PartialVideoAsset>> {
        val playbackIds = videos.map(VideoAsset::playbackId).toList()
        val playbacksById = playbackRepository.find(playbackIds)

        return videos.mapNotNull { video ->
            playbacksById[video.playbackId]?.let { playback ->
                if (playback.duration != video.duration) {
                    Pair(video.assetId, PartialVideoAsset(duration = playback.duration))
                } else {
                    null
                }
            }
        }.toList()
    }
}