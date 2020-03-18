package com.boclips.search.service.domain.common

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.SearchQuery

interface IndexReader<M, Q : SearchQuery<M>> {
    fun search(searchRequest: PaginatedSearchRequest<Q>): List<String>
    fun count(query: Q): Counts
}
