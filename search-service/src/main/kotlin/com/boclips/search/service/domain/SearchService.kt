package com.boclips.search.service.domain

interface SearchService {
    fun resetIndex()
    fun upsert(video: VideoMetadata) = upsert(sequenceOf(video))
    fun upsert(videos: Sequence<VideoMetadata>)
    fun search(query: String): List<String>
    fun removeFromSearch(videoId: String)
}
