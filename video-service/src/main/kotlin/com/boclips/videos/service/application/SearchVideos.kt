package com.boclips.videos.service.application

import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.presentation.resources.VideoResource

class SearchVideos(val searchService: SearchService) {
    fun execute(query: String?): List<VideoResource> {
        query ?: throw QueryValidationException()

        val videos = searchService.search(query)

        return videos.map { VideoResource.from(it) }
    }
}