package com.boclips.videos.service.domain.service.suggestions

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.suggestions.NewSuggestions
import com.boclips.videos.service.domain.model.suggestions.request.ChannelsSuggestionsRequest
import mu.KLogging

class NewSuggestionsRetrievalService(
    private val channelIndex: ChannelIndex
) {
    companion object : KLogging()

    fun findSuggestions(request: ChannelsSuggestionsRequest): NewSuggestions {
        val searchRequest = SearchRequestWithoutPagination(
            query = request.toQuery()
        )

        val results = channelIndex.search(searchRequest)

        val channels = results.elements.map {
            ChannelSuggestion(
                name = it.name,
                id = ChannelId(it.id)
            )
        }

        return NewSuggestions(
            channels = channels
        )
    }
}
