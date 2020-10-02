package com.boclips.videos.service.application.search

import com.boclips.videos.service.common.ResultsSuggestions
import com.boclips.videos.service.domain.model.suggestion.Suggestion
import com.boclips.videos.service.domain.model.suggestion.request.SuggestionRequest
import com.boclips.videos.service.domain.model.user.User
import mu.KLogging

class GetChannelSuggestionByQuery(
    private val channelSuggestionRetrievalService: ChannelSuggestionRetrievalService
) {
    companion object : KLogging()

    operator fun invoke(
        query: String,
        user: User
    ): ResultsSuggestions<Suggestion> {

        val request = SuggestionRequest(
            text = query,
        )

    }
}