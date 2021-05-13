package com.boclips.search.service.domain.common.suggestions

import com.boclips.search.service.domain.common.model.ChannelsSearchRequest
import com.boclips.search.service.domain.search.SearchSuggestionsResults
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.SuggestionsSearchRequest

interface IndexReader<M, Q : SearchQuery<M>> {
    fun search(searchRequest: SuggestionsSearchRequest<Q>): SearchSuggestionsResults
}
