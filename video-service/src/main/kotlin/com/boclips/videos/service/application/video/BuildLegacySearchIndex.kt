package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
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
    open fun execute(): CompletableFuture<Unit> {
        logger.info("Building a legacy index")

        try {
            videoAssetRepository.streamAll { videos ->
                legacySearchService.upsert(videos.map(VideoAssetToLegacyVideoMetadataConverter::convert))
            }
        } catch (e: Exception) {
            logger.error("Error building legacy index", e)
        }

        logger.info("Building a legacy index done.")
        return CompletableFuture.completedFuture(null)
    }
}