package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import java.time.LocalDate

class VideoIndexFake : AbstractInMemoryFake<VideoQuery, VideoMetadata>(),
    IndexReader<VideoMetadata, VideoQuery>,
    IndexWriter<VideoMetadata> {
    override fun upsertMetadata(index: MutableMap<String, VideoMetadata>, item: VideoMetadata) {
        index[item.id] = item.copy()
    }

    override fun idsMatching(index: MutableMap<String, VideoMetadata>, query: VideoQuery): List<String> {
        val phrase = query.phrase

        val releaseDateFrom: LocalDate = query.releaseDateFrom ?: LocalDate.MIN
        val releaseDateTo: LocalDate = query.releaseDateTo ?: LocalDate.MAX

        return index
            .filter { entry ->
                query.ids.isNullOrEmpty() || query.ids.contains(entry.value.id)
            }
            .filter { entry ->
                query.permittedVideoIds.isNullOrEmpty() || query.permittedVideoIds.contains(entry.value.id)
            }
            .filter { entry ->
                query.deniedVideoIds.isNullOrEmpty() || !query.deniedVideoIds.contains(entry.value.id)
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
                if (query.includedType.isEmpty()) true else query.includedType.contains(entry.value.type)
            }
            .filter { entry ->
                if (query.excludedType.isEmpty()) true else !query.excludedType.contains(entry.value.type)
            }.filter { entry ->
                if (query.excludedContentPartnerIds.isEmpty()) true
                else !query.excludedContentPartnerIds.contains(entry.value.contentPartnerId)
            }
            .filter { entry ->
                if (query.durationRanges.isNullOrEmpty()) {
                    return@filter true
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
                if (query.ageRangeMin == null && query.ageRangeMax == null) {
                    return@filter true
                }

                return@filter (entry.value.ageRangeMin == query.ageRangeMin && entry.value.ageRangeMax == query.ageRangeMax)
            }
            .filter { entry ->
                if (query.ageRanges.isNullOrEmpty()) {
                    return@filter true
                }

                return@filter query.ageRanges.any { ageRange ->
                    AgeRange(entry.value.ageRangeMin, entry.value.ageRangeMax) == ageRange
                }
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
                query.active?.let { entry.value.deactivated != it } ?: true
            }
            .filter { entry ->
                if (query.channelNames.isNotEmpty())
                    query.channelNames.contains(entry.value.contentProvider)
                else true
            }
            .filter { entry ->
                query.isEligibleForStream?.let { entry.value.eligibleForStream == it } ?: true
            }.filter { entry ->
                if (query.attachmentTypes.isEmpty()) {
                    true
                } else {
                    query.attachmentTypes.any { queryAttachmentType ->
                        entry.value.attachmentTypes?.any { videoAttachments ->
                            videoAttachments.contains(queryAttachmentType)
                        } ?: true
                    }
                }
            }
            .map { video -> video.key }
    }
}
