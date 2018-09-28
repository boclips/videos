package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.QueryValidationException
import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.event.RequestId
import com.boclips.videos.service.presentation.video.VideoResource

class SearchVideos(val searchService: SearchService, val requestId: RequestId) {
    fun execute(query: String?): SearchResultsResource {
        query ?: throw QueryValidationException()

        val videos = searchService.search(query)

        val videoResources = videos.map { VideoResource.from(it) }

        return SearchResultsResource(searchId = requestId.id!!, query = query, videos = videoResources)
    }

    fun get(id: String): VideoResource {
        val video = searchService.findById(id) ?: throw VideoNotFoundException()

        return VideoResource.from(video)
    }
}
