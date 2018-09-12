package com.boclips.videos.service.application

import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.search.SearchService
import com.boclips.videos.service.presentation.resources.VideoResource

class SearchVideos(val videoService: VideoService) {
    fun execute(query: String?): List<VideoResource> {
        query ?: throw QueryValidationException()

        val videos = videoService.find(query)

        return videos.map { VideoResource.from(it) }
    }
}