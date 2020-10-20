package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.search.ChannelSuggestionResource
import com.boclips.videos.api.response.search.SubjectSuggestionResource
import com.boclips.videos.api.response.search.SuggestionsResource
import com.boclips.videos.service.common.SuggestionsResults

class SuggestionToResourceConverter {
    fun convert(query: String, suggestions: SuggestionsResults): SuggestionsResource {
        return SuggestionsResource(
            suggestionTerm = query,
            channels = suggestions.channels.map {
                ChannelSuggestionResource(
                    id = it.id.value,
                    name = it.name,
                    _links = null
                )
            },
            subjects = suggestions.subjects.map {
                SubjectSuggestionResource(
                    id = it.id.value,
                    name = it.name,
                    _links = null
                )
            },
            _links = null
        )
    }
}