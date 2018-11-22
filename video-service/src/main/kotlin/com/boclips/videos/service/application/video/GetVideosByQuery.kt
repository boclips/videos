package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.presentation.video.VideosResource
import mu.KLogging

class GetVideosByQuery(
        private val videoService: VideoService,
        private val videoToResourceConverter: VideoToResourceConverter
) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 100
        const val DEFAULT_PAGE_INDEX = 0
    }

    fun execute(query: String?, pageIndex: Int?, pageSize: Int?): VideosResource {
        val (validPageSize, validPageNumber) = setDefaults(pageSize, pageIndex)

        query ?: throw QueryValidationException()
        if (validPageSize > DEFAULT_PAGE_SIZE) throw IllegalArgumentException()
        if (validPageSize <= 0) throw IllegalArgumentException()
        if (validPageNumber < 0) throw IllegalArgumentException()

        val videoSearchQuery = VideoSearchQuery(text = query, pageIndex = validPageNumber, pageSize = validPageSize)
        val videoCount = videoService.count(videoSearchQuery)
        logger.info { "Found $videoCount videos for query $videoSearchQuery" }
        val videos: List<Video> = videoService.search(videoSearchQuery)
        logger.info { "Return ${videos.size} results for query $videoSearchQuery" }

        return videoToResourceConverter.convert(videos, videoCount)
    }

    private fun setDefaults(pageSize: Int?, pageIndex: Int?): Pair<Int, Int> {
        val validPageSize = pageSize ?: DEFAULT_PAGE_SIZE
        val validPageIndex = pageIndex ?: DEFAULT_PAGE_INDEX
        return Pair(validPageSize, validPageIndex)
    }
}