package com.boclips.videos.service.application.video

import com.boclips.videos.service.presentation.video.VideoResource

data class SearchResultsResource(
        val query: String,
        val searchId: String,
        val videos: List<VideoResource>
)
