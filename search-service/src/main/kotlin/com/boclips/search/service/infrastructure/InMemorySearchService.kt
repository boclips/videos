package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.VideoMetadata

class InMemorySearchService : GenericSearchService<VideoMetadata> {
    private val index = mutableMapOf<String, VideoMetadata>()

    override fun count(query: Query): Long = idsMatching(query).size.toLong()

    override fun search(searchRequest: PaginatedSearchRequest): List<String> = idsMatching(searchRequest.query)
            .drop(searchRequest.startIndex)
            .take(searchRequest.windowSize)

    private fun idsMatching(query: Query): List<String> {
        val (phrase, ids) = query
        return when {
            !ids.isEmpty() -> index.filter { ids.contains(it.key) }
                    .map { video -> video.key }
            else -> index
                    .filter { entry ->
                        entry.value.title.contains(phrase!!, ignoreCase = true)
                                || entry.value.description.contains(phrase, ignoreCase = true)
                                || entry.value.contentProvider.contains(phrase, ignoreCase = true)
                    }
                    .filter { entry ->
                        entry.value.tags.containsAll(query.includeTags)
                    }
                    .map { video -> video.key }
        }
    }

    override fun removeFromSearch(videoId: String) {
        index.remove(videoId)
    }

    override fun upsert(videos: Sequence<VideoMetadata>) {
        videos.forEach { video ->
            index[video.id] = video.copy()
        }
    }

    override fun resetIndex() {
        index.clear()
    }
}