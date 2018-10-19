package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.domain.VideoMetadata

class InMemorySearchService : SearchService {

    override fun resetIndex() {
        index.clear()
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

    override fun upsert(videos: Sequence<VideoMetadata>) {
        videos.forEach { video ->
            index[video.id] = listOf(video.title, video.description).joinToString(separator = "\n")
        }
    }
}
