package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.*
import kotlin.reflect.full.memberProperties

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
                    .filterValues(filter(query.filters))
                    .map { video -> video.key }
        }
    }

    private fun filter(filters: List<Filter>) = { video: VideoMetadata -> filters.all(this.filter(video)) }

    private fun filter(video: VideoMetadata) = { filter: Filter ->
        val property = VideoMetadata::class.memberProperties.find { it.name == filter.field }!!
        property.get(video) == filter.value
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