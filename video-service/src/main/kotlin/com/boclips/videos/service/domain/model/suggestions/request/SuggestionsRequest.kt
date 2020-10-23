package com.boclips.videos.service.domain.model.suggestions.request

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.request.AccessRuleQueryConverter

class SuggestionsRequest(
    val text: String,
) {
    fun <T> toQuery(videoAccess: VideoAccess? = null): SuggestionQuery<T> {
        return SuggestionQuery(
            phrase = text,
            accessRuleQuery = videoAccess?.let { AccessRuleQueryConverter.toSuggestionAccessRuleQuery(it) }
        )
    }
}
