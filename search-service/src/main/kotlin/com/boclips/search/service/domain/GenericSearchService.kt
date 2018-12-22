package com.boclips.search.service.domain


interface GenericSearchService<T> {
    fun resetIndex()
    fun upsert(videos: Sequence<T>)
    fun search(searchRequest: PaginatedSearchRequest): List<String>
    fun count(query: Query): Long
    fun removeFromSearch(videoId: String)
}
