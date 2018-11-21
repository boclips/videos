package com.boclips.search.service.domain


interface SearchService {
    fun resetIndex()
    fun upsert(video: VideoMetadata) = upsert(sequenceOf(video))
    fun upsert(videos: Sequence<VideoMetadata>)
    fun search(searchRequest: PaginatedSearchRequest): List<String>
    fun count(query: String): Long
    fun removeFromSearch(videoId: String)
}
