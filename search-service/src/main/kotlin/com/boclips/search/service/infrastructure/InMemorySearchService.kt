package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.VideoMetadata

class InMemorySearchService : GenericSearchService<VideoMetadata> {
    private val index = mutableMapOf<String, String>()

    override fun count(query: Query): Long = idsMatching(query).size.toLong()

    override fun search(searchRequest: PaginatedSearchRequest): List<String> = idsMatching(searchRequest.query)
            .drop(searchRequest.startIndex)
            .take(searchRequest.windowSize)

    private fun idsMatching(query: Query): List<String> {
        val (phrase, ids) = query
        return when {
            ids != null -> index.filter { ids.contains(it.key) }
            else -> index
                    .filter { text -> text.value.contains(phrase!!, ignoreCase = true) }
        }.map { video -> video.key }
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
