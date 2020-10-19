package com.boclips.videos.service.application.search

import com.boclips.videos.service.common.SuggestionsResults
import com.boclips.videos.service.domain.model.user.User

class FindNewSuggestions(
    private val findNewSuggestionsByQuery: FindNewSuggestionsByQuery
) {
    fun byQuery(
        query: String,
        user: User
    ): SuggestionsResults {
        return findNewSuggestionsByQuery(
            query = query,
            user = user
        )
    }
}
