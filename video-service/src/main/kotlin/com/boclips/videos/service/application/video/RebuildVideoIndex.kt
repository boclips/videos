package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.model.video.VideoFilter.IsSearchable
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildVideoIndex(
    private val videoRepository: VideoRepository,
    private val videoSearchService: VideoSearchService
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Starting a full reindex")
        val future = CompletableFuture<Unit>()

        try {
            videoRepository.streamAll(IsSearchable) { videos ->
                videoSearchService.safeRebuildIndex(videos, notifier)
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
