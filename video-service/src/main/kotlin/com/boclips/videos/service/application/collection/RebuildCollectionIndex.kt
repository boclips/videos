package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildCollectionIndex(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Starting a full reindex")
        val future = CompletableFuture<Unit>()

        try {
            collectionRepository.streamAllPublic { collections ->
                collectionSearchService.safeRebuildIndex(collections, notifier)
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
