package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.service.SearchService
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildSearchIndex(
        private val videoAssetRepository: VideoAssetRepository,
        private val searchService: SearchService
) {
    companion object : KLogging()

    @Async
    open fun execute(): CompletableFuture<Unit> {
        logger.info("Starting a full reindex")

        try {
            videoAssetRepository.streamAll { videos ->
                searchService.safeRebuildIndex(videos)
            }
        } catch (e: Exception) {
            logger.error("Error reindexing", e)
        }

        logger.info("Full reindex done")
        return CompletableFuture.completedFuture(null)
    }
}