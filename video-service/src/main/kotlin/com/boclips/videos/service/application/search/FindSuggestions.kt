package com.boclips.videos.service.application.search

import com.boclips.videos.service.common.SuggestionsResults
import com.boclips.videos.service.domain.model.user.User

class FindSuggestions(
    private val findSuggestionsByQuery: FindSuggestionsByQuery
) {
    fun byQuery(
        query: String,
        user: User
    ): SuggestionsResults {
        return findSuggestionsByQuery(
            query = query,
            user = user
        )
    }
}
