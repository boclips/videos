package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.resources.VideoResource
import org.springframework.hateoas.Resource

data class SearchResponse(
        val searchId: String,
        val query: String,
        val videos: List<Resource<VideoResource>>
)
