package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.domain.service.TeacherContentFilter
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.video.VideoMetadataConverter
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildSearchIndex(
        private val videoService: VideoService,
        private val searchService: SearchService,
        private val teacherContentFilter: TeacherContentFilter
) {

    @Async
    open fun execute(): CompletableFuture<Unit> {
        searchService.resetIndex()

        videoService.findAllVideos { videos ->
            val videosToIndex = videos
                    .filter { video -> teacherContentFilter.showInTeacherProduct(video) }
                    .map { video -> VideoMetadataConverter.convert(video) }
            searchService.upsert(videosToIndex)
        }

        return CompletableFuture.completedFuture(null)
    }
}