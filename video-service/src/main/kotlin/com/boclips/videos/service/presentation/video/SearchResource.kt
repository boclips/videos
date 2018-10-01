package com.boclips.videos.service.presentation.video

import org.springframework.hateoas.Resource

data class SearchResource(
        val query: String,
        val searchId: String,
        val videos: List<Resource<VideoResource>>
)
