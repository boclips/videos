package com.boclips.search.service.domain

abstract class SearchServiceAdapter<T>(val inner: GenericSearchService<VideoMetadata>) : GenericSearchService<T> {


    override fun resetIndex() {
        inner.resetIndex()
    }

    override fun upsert(videos: Sequence<T>) {
        inner.upsert(videos.map(::convert))
    }

    override fun search(searchRequest: PaginatedSearchRequest): List<String> {
        return inner.search(searchRequest)
    }

    override fun count(query: String): Long {
       return inner.count(query)
    }

    override fun removeFromSearch(videoId: String) {
        inner.removeFromSearch(videoId)
    }

    abstract fun convert(document: T): VideoMetadata
}