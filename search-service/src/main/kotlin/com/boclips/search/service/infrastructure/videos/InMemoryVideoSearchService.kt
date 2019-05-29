package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.AdminSearchService
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.videos.model.SortOrder
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.VideoSearchService
import java.time.LocalDate

class InMemoryVideoSearchService : VideoSearchService, AdminSearchService<VideoMetadata> {
    private val index = mutableMapOf<String, VideoMetadata>()

    override fun count(videoQuery: VideoQuery): Long = idsMatching(videoQuery).size.toLong()

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        val idsMatching = idsMatching(searchRequest.query)

        return sort(idsMatching, searchRequest.query)
            .drop(searchRequest.startIndex)
            .take(searchRequest.windowSize)
    }

    private fun sort(ids: List<String>, videoQuery: VideoQuery): List<String> {
        videoQuery.sort ?: return ids

        val sortedIds = ids.sortedBy {
            val value: Comparable<*> = videoQuery.sort.fieldName.get(index[it]!!)
            /**
             * Kotlin isn't happy about the * to Any cast.. This is the safest way we can coerce the type without
             * littering the entire code base with the Sort generic type.
             *
             * We cannot define sort.fieldName as a Comparable<Any> as it won't then allow us to reference Comparables
             */
            @Suppress("UNCHECKED_CAST")
            value as Comparable<Any>
        }

        return when (videoQuery.sort.order) {
            SortOrder.ASC -> sortedIds
            SortOrder.DESC -> sortedIds.reversed()
        }
    }

    private fun idsMatching(videoQuery: VideoQuery): List<String> {
        val (phrase, ids) = videoQuery

        val minDuration: Long = if (videoQuery.minDuration != null) videoQuery.minDuration.seconds else 0
        val maxDuration: Long = if (videoQuery.maxDuration != null) videoQuery.maxDuration.seconds else Long.MAX_VALUE

        val releaseDateFrom: LocalDate = videoQuery.releaseDateFrom ?: LocalDate.MIN
        val releaseDateTo: LocalDate = videoQuery.releaseDateTo ?: LocalDate.MAX

        return when {
            ids.isNotEmpty() -> index.filter { ids.contains(it.key) }
                .map { video -> video.key }
            else -> index
                .filter { entry ->
                    entry.value.title.contains(phrase!!, ignoreCase = true)
                        || entry.value.description.contains(phrase, ignoreCase = true)
                        || entry.value.contentProvider.contains(phrase, ignoreCase = true)
                        || entry.value.transcript?.contains(phrase, ignoreCase = true) ?: false
                }
                .filter { entry ->
                    entry.value.tags.containsAll(videoQuery.includeTags)
                }
                .filter { entry ->
                    entry.value.durationSeconds.let { (minDuration..maxDuration).contains(it) }
                }
                .filter { entry ->
                    entry.value.tags.none { videoQuery.excludeTags.contains(it) }
                }
                .filter { entry ->
                    videoQuery.source?.let { it == entry.value.source } ?: true
                }.filter { entry ->
                    (releaseDateFrom.toEpochDay()..releaseDateTo.toEpochDay()).contains(entry.value.releaseDate.toEpochDay())
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
