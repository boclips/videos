package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import java.time.LocalDate

class VideoSearchServiceFake : AbstractInMemoryFake<VideoQuery, VideoMetadata>(),
    IndexReader<VideoMetadata, VideoQuery>,
    IndexWriter<VideoMetadata> {
    override fun upsertMetadata(index: MutableMap<String, VideoMetadata>, item: VideoMetadata) {
        index[item.id] = item.copy()
    }

    override fun idsMatching(index: MutableMap<String, VideoMetadata>, query: VideoQuery): List<String> {
        val phrase = query.phrase
        val idsToLookup = query.ids

        val minDuration: Long = if (query.minDuration != null) query.minDuration.seconds else 0
        val maxDuration: Long = if (query.maxDuration != null) query.maxDuration.seconds else Long.MAX_VALUE

        val releaseDateFrom: LocalDate = query.releaseDateFrom ?: LocalDate.MIN
        val releaseDateTo: LocalDate = query.releaseDateTo ?: LocalDate.MAX

        return when {
            idsToLookup.isNotEmpty() -> index.filter {
                val permittedIdsToLookup =
                    if (query.permittedVideoIds.isNullOrEmpty()) idsToLookup else idsToLookup.intersect(query.permittedVideoIds)
                permittedIdsToLookup.contains(it.key)
            }
                .map { video -> video.key }
            else -> index
                .filter { entry ->
                    query.permittedVideoIds.isNullOrEmpty() || query.permittedVideoIds.contains(entry.value.id)
                }
                .filter { entry ->
                    entry.value.title.contains(phrase, ignoreCase = true)
                        || entry.value.description.contains(phrase, ignoreCase = true)
                        || entry.value.contentProvider.contains(phrase, ignoreCase = true)
                        || entry.value.transcript?.contains(phrase, ignoreCase = true) ?: false
                }
                .filter { entry ->
                    entry.value.tags.containsAll(query.includeTags)
                }
                .filter { entry ->
                    if (query.type.isEmpty()) true else query.type.contains(entry.value.type)
                }
                .filter { entry ->
                    entry.value.durationSeconds.let { (minDuration..maxDuration).contains(it) }
                }
                .filter { entry ->
                    entry.value.tags.none { query.excludeTags.contains(it) }
                }
                .filter { entry ->
                    query.source?.let { it == entry.value.source } ?: true
                }.filter { entry ->
                    (releaseDateFrom.toEpochDay()..releaseDateTo.toEpochDay()).contains(entry.value.releaseDate.toEpochDay())
                }.filter { entry ->
                    if (query.ageRangeMin == null && query.ageRangeMax == null) {
                        true
                    } else if (query.ageRangeMin == null) {
                        entry.value.ageRangeMin?.let { videoMin ->
                            query.ageRangeMax != null && videoMin <= query.ageRangeMax
                        } ?: false
                    } else {
                        entry.value.ageRangeMin?.let { videoMin ->
                            val videoMax = entry.value.ageRangeMax
                            val queryMin = query.ageRangeMin
                            val queryMax = query.ageRangeMax

                            compareAgeRanges(videoMin, queryMin, videoMax, queryMax)
                        } ?: false
                    }

                }.filter { entry ->
                    if (query.subjectIds.isEmpty()) {
                        true
                    } else {
                        query.subjectIds.any { querySubject ->
                            entry.value.subjects.any { videoSubject ->
                                videoSubject.id.contains(querySubject)
                            }
                        }
                    }
                }.filter { entry ->
                    query.promoted?.let { entry.value.promoted == it } ?: true
                }.filter { entry ->
                    if (query.contentPartnerNames.isNotEmpty())
                        query.contentPartnerNames.contains(entry.value.contentProvider)
                    else true
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
