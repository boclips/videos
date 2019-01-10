package com.boclips.search.service.domain

abstract class SearchServiceAdapter<T>(
        private val queryService: GenericSearchService,
        private val adminService: GenericSearchServiceAdmin<VideoMetadata>) : GenericSearchService, GenericSearchServiceAdmin<T> {
    override fun resetIndex() {
        adminService.resetIndex()
    }

    override fun upsert(videos: Sequence<T>) {
        adminService.upsert(videos.map(::convert))
    }

    override fun search(searchRequest: PaginatedSearchRequest): List<String> {
        return queryService.search(searchRequest)
    }

    override fun count(query: Query): Long {
        return queryService.count(query)
    }

    override fun removeFromSearch(videoId: String) {
        adminService.removeFromSearch(videoId)
    }

    abstract fun convert(document: T): VideoMetadata
}
