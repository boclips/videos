package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.video.SortKey
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.VideoController.Companion.MAX_PAGE_SIZE
import mu.KLogging

class GetVideosByQuery(
    private val videoService: VideoService,
    private val eventService: EventService,
    private val searchQueryConverter: SearchQueryConverter
) {
    companion object : KLogging()

    operator fun invoke(
        query: String,
        sortBy: SortKey?,
        includeTags: List<String>,
        excludeTags: List<String>,
        minDurationString: String?,
        maxDurationString: String?,
        releasedDateFrom: String?,
        releasedDateTo: String?,
        pageSize: Int,
        pageNumber: Int,
        source: String?,
        ageRangeMin: Int?,
        ageRangeMax: Int?,
        subjects: Set<String>
    ): Page<Video> {
        validatePageSize(pageSize)
        validatePageNumber(pageNumber)

        val videoSearchQuery = VideoSearchQuery(
            text = query,
            sortBy = sortBy,
            pageIndex = pageNumber,
            pageSize = pageSize,
            includeTags = includeTags,
            excludeTags = excludeTags,
            minDuration = searchQueryConverter.convertDuration(minDurationString),
            maxDuration = searchQueryConverter.convertDuration(maxDurationString),
            source = searchQueryConverter.convertSource(source),
            releaseDateFrom = searchQueryConverter.convertDate(releasedDateFrom),
            releaseDateTo = searchQueryConverter.convertDate(releasedDateTo),
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            subjects = subjects
        )

        val totalVideos = videoService.count(videoSearchQuery = videoSearchQuery)
        logger.info { "Found $totalVideos videos for query $videoSearchQuery" }

        val videos: List<Video> = videoService.search(videoSearchQuery)
        logger.info { "Return ${videos.size} out of $pageSize results for query $videoSearchQuery" }

        eventService.saveSearchEvent(
            query = query,
            pageIndex = pageNumber,
            pageSize = pageSize,
            totalResults = totalVideos,
            pageVideoIds = videos.map { it.videoId.value }
        )

        return Page(
            elements = videos.asIterable(),
            pageInfo = PageInfo(
                hasMoreElements = (pageNumber + 1) * pageSize < totalVideos,
                totalElements = totalVideos,
                pageRequest = PageRequest(page = pageNumber, size = pageSize)
            )
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
