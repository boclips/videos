package com.boclips.search.service.domain

interface GenericSearchService {
    fun search(searchRequest: PaginatedSearchRequest): List<String>
    fun count(query: Query): Long
}
