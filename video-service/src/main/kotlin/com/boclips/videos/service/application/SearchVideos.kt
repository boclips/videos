package com.boclips.videos.service.application

import com.boclips.videos.service.application.exceptions.QueryValidationException
import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.resources.VideoResource

class SearchVideos(val videoService: VideoService) {
    fun execute(query: String?): SearchResultsResource {
        query ?: throw QueryValidationException()

        val results = videoService.search(query)

        val videoResources = results.videos.map { VideoResource.from(it) }

        return SearchResultsResource(searchId = results.searchId, query = results.query, videos = videoResources)
    }

    fun get(id: String): VideoResource {
        val video = videoService.findById(id) ?: throw VideoNotFoundException()

        return VideoResource.from(video)
    }
}
