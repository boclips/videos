package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.AbstractInMemorySearchService
import java.time.LocalDate

class InMemoryVideoReadSearchService : AbstractInMemorySearchService<VideoQuery, VideoMetadata>(), ReadSearchService<VideoMetadata, VideoQuery>, WriteSearchService<VideoMetadata> {
    override fun upsertMetadata(index: MutableMap<String, VideoMetadata>, item: VideoMetadata) {
        index[item.id] = item.copy()
    }

    override fun idsMatching(index: MutableMap<String, VideoMetadata>, videoQuery: VideoQuery): List<String> {
        val phrase = videoQuery.phrase
        val ids = videoQuery.ids

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

}
