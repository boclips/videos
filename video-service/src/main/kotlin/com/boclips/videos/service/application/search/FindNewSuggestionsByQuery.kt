package com.boclips.videos.service.application.search

import com.boclips.videos.service.common.SuggestionsResults
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.suggestions.request.ChannelsSuggestionsRequest
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.suggestions.NewSuggestionsRetrievalService

class FindNewSuggestionsByQuery(
    private val newSuggestionsRetrievalService: NewSuggestionsRetrievalService
) {
    operator fun invoke(
        query: String,
        user: User,
    ): SuggestionsResults<ChannelSuggestion> {
        val channelsRequest = ChannelsSuggestionsRequest(
            text = query,
        )

        val channelsSuggestionsResponse = newSuggestionsRetrievalService.findSuggestions(channelsRequest, user.accessRules.videoAccess)

        return SuggestionsResults(
            channels = channelsSuggestionsResponse.channels.asIterable()
        )
    }
}
