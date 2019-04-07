package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.videos.service.domain.model.asset.VideoAssetFilter.IsSearchable
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.service.video.SearchService
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildSearchIndex(
    private val videoAssetRepository: VideoAssetRepository,
    private val searchService: SearchService
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Starting a full reindex")
        val future = CompletableFuture<Unit>()

        try {
            videoAssetRepository.streamAll(IsSearchable) { videos ->
                searchService.safeRebuildIndex(videos, notifier)
            }

            logger.info("Full reindex done")
            future.complete(null)
        } catch (e: Exception) {
            logger.error("Error reindexing", e)
            future.completeExceptionally(e)
        }

        return future
    }
}
