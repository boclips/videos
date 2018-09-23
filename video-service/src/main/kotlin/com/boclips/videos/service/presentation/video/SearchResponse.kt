package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.Resource

data class SearchResponse(
        val searchId: String,
        val query: String,
        val videos: List<Resource<VideoResource>>
)
