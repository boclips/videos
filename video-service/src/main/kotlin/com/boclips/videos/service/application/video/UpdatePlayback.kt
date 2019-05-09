package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetFilter.IsSearchable
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class UpdatePlayback(
    private val videoAssetRepository: VideoAssetRepository,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Starting a refresh of video playbacks")
        val future = CompletableFuture<Unit>()

        try {
            refreshPlaybacks(notifier)

            logger.info("Completed refresh of video playbacks")
            future.complete(null)
        } catch (e: Exception) {
            logger.error("Error refreshing video playbacks", e)
            future.completeExceptionally(e)
        }

        return future
    }

    private fun refreshPlaybacks(notifier: ProgressNotifier?) {
        videoAssetRepository.streamAll(IsSearchable) { sequence ->
            var batch = 0

            sequence.chunked(size = 50).forEach { videos ->
                notifier?.send("Processing playback updates batch ${batch++}")

                val updatesByAssetId = playbackUpdates(videos)
                if (updatesByAssetId.isNotEmpty()) {
                    logger.info("Updating playback for ${updatesByAssetId.size} videos")
                    notifier?.send("Updating playback for ${updatesByAssetId.size} videos")
                    videoAssetRepository.bulkUpdate(updatesByAssetId)
                }
            }
        }
    }

    private fun playbackUpdates(videos: List<VideoAsset>): List<VideoUpdateCommand.ReplacePlayback> {
        val playbackIds = videos.map(VideoAsset::playbackId).toList()
        val playbacksById = playbackRepository.find(playbackIds)

        return videos.mapNotNull { video: VideoAsset ->
            playbacksById[video.playbackId]?.let { playback ->
                VideoUpdateCommand.ReplacePlayback(
                    assetId = video.assetId,
                    playback = playback
                )
            }
        }.toList()
    }
}
