package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.domain.SearchableVideoMetadata

class InMemorySearchService : SearchService {
    private val index = mutableMapOf<String, String>()

    override fun upsert(video: SearchableVideoMetadata) {
        val searchableText = listOf(video.title, video.description).joinToString(separator = "\n")
        index[video.id] = searchableText
    }

    override fun search(query: String): List<String> {
        return index
                .filter { text -> text.value.contains(query, ignoreCase = true) }
                .map { it.key }
    }

    override fun removeFromSearch(videoId: String) {
        index.remove(videoId)
    }

    fun clear() {
        index.clear()
    }
}
