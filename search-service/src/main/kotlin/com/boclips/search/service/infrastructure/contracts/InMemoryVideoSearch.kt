package com.boclips.search.service.infrastructure.contracts

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import java.time.LocalDate

class InMemoryVideoSearch : AbstractInMemorySearch<VideoQuery, VideoMetadata>(),
    IndexReader<VideoMetadata, VideoQuery>,
    IndexWriter<VideoMetadata> {
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
                }.filter { entry ->
                    if (videoQuery.ageRangeMin == null && videoQuery.ageRangeMax == null) {
                        true
                    } else if (videoQuery.ageRangeMin == null) {
                        entry.value.ageRangeMin?.let { videoMin ->
                            videoQuery.ageRangeMax != null && videoMin <= videoQuery.ageRangeMax
                        } ?: false
                    } else {
                        entry.value.ageRangeMin?.let { videoMin ->
                            val videoMax = entry.value.ageRangeMax
                            val queryMin = videoQuery.ageRangeMin
                            val queryMax = videoQuery.ageRangeMax

                            compareAgeRanges(videoMin, queryMin, videoMax, queryMax)
                        } ?: false
                    }

                }.filter { entry ->
                    if (videoQuery.subjects.isEmpty()) {
                        true
                    } else {
                        videoQuery.subjects.any { querySubject ->
                            entry.value.subjects.any { videoSubject ->
                                videoSubject.contains(querySubject)
                            }
                        }
                    }
                }
                .map { video -> video.key }
        }
    }

    private fun compareAgeRanges(videoMin: Int, queryMin: Int, videoMax: Int?, queryMax: Int?): Boolean {
        return (
            videoMin <= queryMin
                && (videoMax == null || videoMax >= queryMin)
                || videoMin >= queryMin
                && (videoMax == null || queryMax == null || videoMin <= queryMax)
            )
    }
}
