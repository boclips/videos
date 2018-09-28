package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.QueryValidationException
import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.RequestId
import com.boclips.videos.service.presentation.video.VideoResource

class SearchVideos(val videoService: VideoService, val requestId: RequestId) {
    fun execute(query: String?): SearchResultsResource {
        query ?: throw QueryValidationException()

        val videos = videoService.search(query)

        val videoResources = videos.map { VideoResource.from(it) }

        return SearchResultsResource(searchId = requestId.id!!, query = query, videos = videoResources)
    }

    fun get(id: String): VideoResource {
        val video = videoService.findById(id) ?: throw VideoNotFoundException()

        return VideoResource.from(video)
    }
}
