package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.video.VideoAssetToLegacyVideoMetadataConverter
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class BuildLegacySearchIndex(
    private val videoAssetRepository: VideoAssetRepository,
    private val legacySearchService: LegacySearchService
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Building a legacy index")
        val future = CompletableFuture<Unit>()

        try {
            videoAssetRepository.streamAllSearchable { videos ->
                val videoAssets = videos
                    .filter { it.title.isNotEmpty() }
                    .filter { it.playbackId.type == PlaybackProviderType.KALTURA }
                    .map(VideoAssetToLegacyVideoMetadataConverter::convert)

                legacySearchService.upsert(videoAssets, notifier)
            }

            logger.info("Building a legacy index done.")
            future.complete(null)
        } catch (e: Exception) {
            logger.error("Error building legacy index", e)
            future.completeExceptionally(e)
        }

        return future
    }
}