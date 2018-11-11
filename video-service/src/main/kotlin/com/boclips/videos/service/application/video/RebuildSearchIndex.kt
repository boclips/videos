package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.ContentFilters
import com.boclips.videos.service.infrastructure.video.VideoMetadataConverter
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

        searchService.resetIndex()

        logger.info("Requesting videos")
        try {
            videoAssetRepository.streamAll { videos ->
                logger.info("Starting to read videos")
                val videosToIndex = videos
                        .filter { video -> ContentFilters.isInTeacherProduct(video) }
                        .map { video -> VideoMetadataConverter.convert(video) }
                logger.info("Passing videos to the search service")
                searchService.upsert(videosToIndex)
            }
        } catch (e: Exception) {
            logger.error("Error reindexing", e)
        }

        logger.info("Full reindex done")
        return CompletableFuture.completedFuture(null)
    }
}