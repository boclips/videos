package com.boclips.search.service.domain

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.model.SearchQuery

interface ReadSearchService<M, Q: SearchQuery<M>> {
    fun search(searchRequest: PaginatedSearchRequest<Q>): List<String>
    fun count(videoQuery: Q): Long
}
