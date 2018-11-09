package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.domain.service.VideoRepository
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.domain.service.filters.TeacherContentFilter
import com.boclips.videos.service.infrastructure.video.VideoMetadataConverter
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildSearchIndex(
        private val videoRepository: VideoRepository,
        private val searchService: SearchService,
        private val teacherContentFilter: TeacherContentFilter
) {
    companion object : KLogging()

    @Async
    open fun execute(): CompletableFuture<Unit> {
        logger.info("Starting a full reindex")

        searchService.resetIndex()

        logger.info("Requesting videos")
        try {
            videoRepository.findAllVideos { videos ->
                logger.info("Starting to read videos")
                val videosToIndex = videos
                        .filter { video -> teacherContentFilter.showInTeacherProduct(video) }
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