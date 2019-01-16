package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.VideoAssetToLegacyVideoMetadataConverter
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class BuildLegacySearchIndex(
        private val videoAssetRepository: VideoAssetRepository,
        private val legacySearchService: LegacySearchService
) {
    companion object : KLogging()

    @Async
    open fun execute(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Building a legacy index")

        try {
            videoAssetRepository.streamAll { videos ->
                val videoAssets = videos
                        .filter { it.playbackId.type == PlaybackProviderType.KALTURA }
                        .map(VideoAssetToLegacyVideoMetadataConverter::convert)

                legacySearchService.upsert(videoAssets, notifier)
            }
        } catch (e: Exception) {
            logger.error("Error building legacy index", e)
        }

        logger.info("Building a legacy index done.")
        return CompletableFuture.completedFuture(null)
    }
}