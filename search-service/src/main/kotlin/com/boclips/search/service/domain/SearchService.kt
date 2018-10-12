package com.boclips.search.service.domain

interface SearchService {
    fun upsert(video: SearchableVideoMetadata)

    fun search(query: String): List<String>
    fun removeFromSearch(videoId: String)
}
