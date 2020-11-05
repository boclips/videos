package com.boclips.search.service.domain.common

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.IndexSearchRequest

interface IndexReader<M, Q : SearchQuery<M>> {
    fun search(searchRequest: IndexSearchRequest<Q>): SearchResults
}
