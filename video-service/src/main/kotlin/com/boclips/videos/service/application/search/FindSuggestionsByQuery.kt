package com.boclips.videos.service.application.search

import com.boclips.videos.service.common.SuggestionsResults
import com.boclips.videos.service.domain.model.suggestions.request.SuggestionsRequest
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.suggestions.SuggestionsRetrievalService

class FindSuggestionsByQuery(
    private val suggestionsRetrievalService: SuggestionsRetrievalService
) {
    operator fun invoke(
        query: String,
        user: User,
    ): SuggestionsResults {

        val suggestionsRequest = SuggestionsRequest(
            text = query,
        )

        val suggestionsResponse = suggestionsRetrievalService.findSuggestions(suggestionsRequest, user.accessRules.videoAccess)

        return SuggestionsResults(
            channels = suggestionsResponse.channels.asIterable(),
            subjects = suggestionsResponse.subjects.asIterable()
        )
    }
}
