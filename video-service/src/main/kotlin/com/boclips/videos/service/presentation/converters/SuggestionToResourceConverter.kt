package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.search.ChannelSuggestionResource
import com.boclips.videos.api.response.search.SubjectSuggestionResource
import com.boclips.videos.api.response.search.SuggestionsResource
import com.boclips.videos.service.common.SuggestionsResults
import com.boclips.videos.service.domain.model.Suggestions
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder

class SuggestionToResourceConverter(
    private val videosLinkBuilder: VideosLinkBuilder
) {
    fun convert(query: String, suggestions: Suggestions): SuggestionsResource {
        return SuggestionsResource(
            suggestionTerm = query,
            channels = suggestions.channels.map {
                ChannelSuggestionResource(
                    id = it.id.value,
                    name = it.name,
                    _links = listOfNotNull(
                        videosLinkBuilder.searchVideosByText(query = query)
                    ).map { link -> link.rel to link }.toMap()
                )
            },
            subjects = suggestions.subjects.map {
                SubjectSuggestionResource(
                    id = it.id.value,
                    name = it.name,
                    _links = listOfNotNull(
                        videosLinkBuilder.searchVideosByText(query = query)
                    ).map { link -> link.rel to link }.toMap()
                )
            },
            _links = null
        )
    }

    fun convertNewSuggestions(query: String, suggestions: SuggestionsResults<ChannelSuggestion>): SuggestionsResource {
        return SuggestionsResource(
            suggestionTerm = query,
            channels = suggestions.channels.map {
                ChannelSuggestionResource(
                    id = it.id.value,
                    name = it.name,
                    _links = null
                )
            },
            subjects = emptyList(),
            _links = null
        )
    }
}
