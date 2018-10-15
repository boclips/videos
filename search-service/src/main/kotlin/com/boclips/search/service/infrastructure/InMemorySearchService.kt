package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.domain.SearchableVideoMetadata

class InMemorySearchService : SearchService {
    override fun createIndex(videos: List<SearchableVideoMetadata>) {
        index.clear()
        videos.forEach { video ->
            insert(video)
        }
    }

    private val index = mutableMapOf<String, String>()

    override fun search(query: String): List<String> {
        return index
                .filter { text -> text.value.contains(query, ignoreCase = true) }
                .map { it.key }
    }

    override fun removeFromSearch(videoId: String) {
        index.remove(videoId)
    }

    private fun insert(video: SearchableVideoMetadata) {
        index[video.id] = listOf(video.title, video.description).joinToString(separator = "\n")
    }
}
