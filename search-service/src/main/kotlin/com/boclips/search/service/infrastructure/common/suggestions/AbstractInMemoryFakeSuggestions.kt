package com.boclips.search.service.infrastructure.common.suggestions

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.common.suggestions.Suggestion
import com.boclips.search.service.domain.common.suggestions.SuggestionsIndexReader
import com.boclips.search.service.domain.search.SearchSuggestionsResults
import com.boclips.search.service.infrastructure.contract.AbstractInMemoryFake

abstract class AbstractInMemoryFakeSuggestions<SUGGESTION_QUERY : SearchQuery<METADATA>, QUERY : SearchQuery<METADATA>, METADATA> :
    AbstractInMemoryFake<QUERY, METADATA>(),
    SuggestionsIndexReader<METADATA, SUGGESTION_QUERY> {
    private var requests: MutableList<SuggestionRequest<SUGGESTION_QUERY>> = mutableListOf()

    override fun getSuggestions(suggestionRequest: SuggestionRequest<SUGGESTION_QUERY>): SearchSuggestionsResults {
        requests.add(suggestionRequest)

        val nameMatching = nameMatching(this.getIndex(), suggestionRequest.query)

        return SearchSuggestionsResults(
            elements = nameMatching.map { it ->
                Suggestion(
                    id = it.id,
                    name = it.name
                )
            }
        )
    }

    abstract fun nameMatching(index: Map<String, METADATA>, query: SUGGESTION_QUERY): List<Suggestion>
}
