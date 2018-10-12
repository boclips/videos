package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.domain.SearchableVideoMetadata

class InMemorySearchService : SearchService {
    private val index = mutableMapOf<String, String>()

    override fun upsert(video: SearchableVideoMetadata) {
        val searchableText = listOf(video.title, video.description).joinToString(separator = "\n")
        index[searchableText] = video.id
    }

    override fun search(query: String): List<String> {
        return index
                .filter { text -> text.key.contains(query, ignoreCase = true) }
                .map { it.value }
    }

    override fun removeFromSearch(videoId: String) {
        val key = index.entries.find { it.value == videoId }!!.key
        index.remove(key)
    }

    fun clear() {
        index.clear()
    }
}
