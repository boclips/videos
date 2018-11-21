package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.domain.VideoMetadata

class InMemorySearchService : SearchService {
    private val index = mutableMapOf<String, String>()

    override fun count(query: String): Long {
       return index.size.toLong()
    }

    override fun resetIndex() {
        index.clear()
    }

    override fun search(searchRequest: PaginatedSearchRequest): List<String> {
        return index
                .filter { text -> text.value.contains(searchRequest.query, ignoreCase = true) }
                .map { it.key }
    }

    override fun removeFromSearch(videoId: String) {
        index.remove(videoId)
    }

    override fun upsert(videos: Sequence<VideoMetadata>) {
        videos.forEach { video ->
            index[video.id] = listOf(video.title, video.description, video.contentProvider).joinToString(separator = "\n")
        }
    }
}
