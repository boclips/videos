package com.boclips.search.service.domain

interface SearchService {
    fun createIndex(videos: List<VideoMetadata>)
    fun search(query: String): List<String>
    fun removeFromSearch(videoId: String)
}
