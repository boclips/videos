package com.boclips.search.service.domain.common.suggestions

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.search.SearchSuggestionsResults

interface SuggestionsIndexReader<M, Q : SearchQuery<M>> {
    fun getSuggestions(suggestionRequest: SuggestionRequest<Q>): SearchSuggestionsResults
}
