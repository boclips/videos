package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.search.ContentPartnerSuggestionResource
import com.boclips.videos.api.response.search.SuggestionDetailsResource
import com.boclips.videos.api.response.search.SuggestionsResource
import com.boclips.videos.api.response.search.SuggestionsWrapperResource
import com.boclips.videos.service.domain.model.Suggestions

class SuggestionToResourceConverter {
    fun convert(suggestions: Suggestions): SuggestionsResource {
        return SuggestionsResource(
            _embedded = SuggestionsWrapperResource(
                suggestions = SuggestionDetailsResource(
                    contentPartners = suggestions.contentPartners.map {
                        ContentPartnerSuggestionResource(
                            name = it,
                            _links = null
                        )
                    }
                )
            ),
            _links = null
        )
    }
}
