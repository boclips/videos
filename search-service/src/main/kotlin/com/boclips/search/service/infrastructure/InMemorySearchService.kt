package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.VideoMetadata

class InMemorySearchService : GenericSearchService<VideoMetadata> {
    private val index = mutableMapOf<String, String>()

    override fun count(query: String): Long {
        return index
                .filter { text -> text.value.contains(query, ignoreCase = true) }
                .size.toLong()
    }

    override fun search(searchRequest: PaginatedSearchRequest): List<String> {
        val videos = index
                .filter { text -> text.value.contains(searchRequest.query, ignoreCase = true) }

        val from = searchRequest.startIndex
        val to = Math.min(searchRequest.startIndex + searchRequest.windowSize, videos.size)

        return videos
                .map { it.key }
                .subList(from, to)
    }

    override fun removeFromSearch(videoId: String) {
        index.remove(videoId)
    }

    override fun upsert(videos: Sequence<VideoMetadata>) {
        videos.forEach { video ->
            index[video.id] = listOf(video.title, video.description, video.contentProvider).joinToString(separator = "\n")
        }
    }

    override fun resetIndex() {
        index.clear()
    }
}
