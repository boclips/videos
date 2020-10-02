package com.boclips.videos.service.domain.model.suggestion.request

import com.boclips.videos.service.domain.model.video.VideoAccess

class SuggestionRequest(
    val text: String,
) {
    fun toQuery(videoAccess: VideoAccess): SuggestionQuery {

    }
}