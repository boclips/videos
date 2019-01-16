package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.*

class InMemorySearchService : GenericSearchService, GenericSearchServiceAdmin<VideoMetadata> {
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
                    .filter { entry ->
                        entry.value.tags.none { query.excludeTags.contains(it) }
                    }
                    .map { video -> video.key }
        }
    }

    override fun removeFromSearch(videoId: String) {
        index.remove(videoId)
    }

    override fun upsert(videos: Sequence<VideoMetadata>, notifier: ProgressNotifier?) {
        videos.forEach { video ->
            index[video.id] = video.copy()
        }
    }

    override fun safeRebuildIndex(videos: Sequence<VideoMetadata>, notifier: ProgressNotifier?) {
        index.clear()
        upsert(videos, notifier)
    }
}