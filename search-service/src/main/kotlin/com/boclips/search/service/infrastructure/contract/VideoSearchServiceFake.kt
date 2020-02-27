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
                    if (query.bestFor.isNullOrEmpty()) true else entry.value.tags.containsAll(query.bestFor)
                }
                .filter { entry ->
                    if (query.type.isEmpty()) true else query.type.contains(entry.value.type)
                }
                .filter { entry ->
                    if (query.durationRanges.isNullOrEmpty()) {
                        return@filter true;
                    }

                    query.durationRanges.forEach { durationRange ->
                        val minSeconds = durationRange.min.seconds
                        val maxSeconds = durationRange.max?.seconds ?: Long.MAX_VALUE
                        if ((minSeconds..maxSeconds).contains(entry.value.durationSeconds)) {
                            return@filter true
                        }
                    }

                    return@filter false
                }
                .filter { entry ->
                    query.source?.let { it == entry.value.source } ?: true
                }.filter { entry ->
                    (releaseDateFrom.toEpochDay()..releaseDateTo.toEpochDay()).contains(entry.value.releaseDate.toEpochDay())
                }.filter { entry ->
                    if (query.subjectIds.isEmpty()) {
                        true
                    } else {
                        query.subjectIds.any { querySubject ->
                            entry.value.subjects.items.any { videoSubject ->
                                videoSubject.id.contains(querySubject)
                            }
                        }
                    }
                }.filter { entry ->
                    query.subjectsSetManually?.let { entry.value.subjects.setManually == it } ?: true
                }.filter { entry ->
                    query.promoted?.let { entry.value.promoted == it } ?: true
                }.filter { entry ->
                    if (query.contentPartnerNames.isNotEmpty())
                        query.contentPartnerNames.contains(entry.value.contentProvider)
                    else true
                }
                .filter { entry ->
                    query.isClassroom?.let { entry.value.isClassroom == it } ?: true
                }
                .map { video -> video.key }
        }
    }
}
