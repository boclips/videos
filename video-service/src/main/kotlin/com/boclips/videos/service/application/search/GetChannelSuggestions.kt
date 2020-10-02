package com.boclips.videos.service.application.search

import com.boclips.videos.service.common.ResultsSuggestions
import com.boclips.videos.service.domain.model.suggestion.Suggestion

class GetChannelSuggestions(
    private val getChannelSuggestionByQuery: GetChannelSuggestionByQuery,
) {
    fun byQuery(
        query: String?
    ): ResultsSuggestions<Suggestion> {
        return getChannelSuggestionByQuery(
            query = query
        )
    }
}