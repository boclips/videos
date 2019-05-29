package com.boclips.search.service.domain

interface SearchService<Q> {
    fun search(searchRequest: PaginatedSearchRequest<Q>): List<String>
    fun count(videoQuery: Q): Long
}
