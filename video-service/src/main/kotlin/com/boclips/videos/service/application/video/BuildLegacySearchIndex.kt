package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.domain.model.video.VideoFilter.IsSearchable
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class BuildLegacySearchIndex(
    private val videoRepository: VideoRepository,
    private val legacySearchService: LegacySearchService
) {
    companion object : KLogging()

    @Async
    open operator fun invoke(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        logger.info("Building a legacy index")
        val future = CompletableFuture<Unit>()

        try {
            videoRepository.streamAll(IsSearchable) { videos ->
                val filteredVideos = videos
                    .filter { it.title.isNotEmpty() }
                    .filter { it.isPlayable() }
                    .filter { it.isBoclipsHosted() }
                    .map(VideoToLegacyVideoMetadataConverter::convert)

                legacySearchService.upsert(filteredVideos, notifier)
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
