package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.VideoController.Companion.MAX_PAGE_SIZE
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.presentation.video.VideosResource
import mu.KLogging

class GetVideosByQuery(
    private val videoService: VideoService,
    private val videoToResourceConverter: VideoToResourceConverter,
    private val eventService: EventService
) {
    companion object : KLogging()

    operator fun invoke(
        query: String,
        includeTags: List<String>,
        excludeTags: List<String>,
        pageSize: Int,
        pageNumber: Int
    ): VideosResource {
        validatePageSize(pageSize)
        validatePageNumber(pageNumber)

        val videoSearchQuery = VideoSearchQuery(
            text = query,
            pageIndex = pageNumber,
            pageSize = pageSize,
            includeTags = includeTags,
            excludeTags = excludeTags
        )

        val totalVideos = videoService.count(videoSearchQuery = videoSearchQuery)
        logger.info { "Found $totalVideos videos for query $videoSearchQuery" }

        val videos: List<Video> = videoService.search(videoSearchQuery)
        logger.info { "Return ${videos.size} out of $pageSize results for query $videoSearchQuery" }

        val videoResources = videoToResourceConverter.fromVideos(videos)

        eventService.saveSearchEvent(
            query = query,
            pageIndex = pageNumber,
            pageSize = pageSize,
            totalResults = totalVideos
        )

        return VideosResource(
            videos = videoResources,
            totalVideos = totalVideos,
            pageNumber = pageNumber,
            pageSize = pageSize
        )
    }

    private fun validatePageNumber(pageNumber: Int) {
        if (pageNumber < 0) throw IllegalArgumentException()
    }

    private fun validatePageSize(pageSize: Int) {
        if (pageSize > MAX_PAGE_SIZE) throw IllegalArgumentException()
        if (pageSize <= 0) throw IllegalArgumentException()
    }
}