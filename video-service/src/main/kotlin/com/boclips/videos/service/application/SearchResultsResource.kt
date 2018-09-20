package com.boclips.videos.service.application

import com.boclips.videos.service.presentation.resources.VideoResource

data class SearchResultsResource(
        val query: String,
        val searchId: String,
        val videos: List<VideoResource>
)
