package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VoiceType
import java.time.LocalDate

class VideoIndexFake :
    AbstractInMemoryFake<VideoQuery, VideoMetadata>(),
    IndexReader<VideoMetadata, VideoQuery>,
    IndexWriter<VideoMetadata> {
    override fun upsertMetadata(index: MutableMap<String, VideoMetadata>, item: VideoMetadata) {
        index[item.id] = item.copy()
    }

    override fun idsMatching(index: MutableMap<String, VideoMetadata>, query: VideoQuery): List<String> {
        val phrase = query.phrase

        val releaseDateFrom: LocalDate = query.userQuery.releaseDateFrom ?: LocalDate.MIN
        val releaseDateTo: LocalDate = query.userQuery.releaseDateTo ?: LocalDate.MAX

        return index
            .filter { entry ->
                query.userQuery.ids.isNullOrEmpty() || query.userQuery.ids.contains(entry.value.id)
            }
            .filter { entry ->
                matchOrFilters(query, entry)
            }
            .filter { entry ->
                query.videoAccessRuleQuery.deniedVideoIds.isNullOrEmpty() || !query.videoAccessRuleQuery.deniedVideoIds.contains(
                    entry.value.id
                )
            }
            .filter { entry ->
                entry.value.title.contains(phrase, ignoreCase = true) ||
                    entry.value.description.contains(phrase, ignoreCase = true) ||
                    entry.value.contentProvider.contains(phrase, ignoreCase = true) ||
                    entry.value.transcript?.contains(phrase, ignoreCase = true) ?: false
            }
            .filter { entry ->
                if (query.userQuery.bestFor.isNullOrEmpty()) true else entry.value.tags.containsAll(query.userQuery.bestFor)
            }
            .filter { entry ->
                filterByIncludedType(query, entry)
            }
            .filter { entry ->
                if (query.videoAccessRuleQuery.excludedTypes.isEmpty()) true else !query.videoAccessRuleQuery.excludedTypes.any {
                    entry.value.types.contains(
                        it
                    )
                }
            }.filter { entry ->
                if (query.videoAccessRuleQuery.excludedContentPartnerIds.isEmpty()) true
                else !query.videoAccessRuleQuery.excludedContentPartnerIds.contains(entry.value.contentPartnerId)
            }
            .filter { entry ->
                if (query.userQuery.durationRanges.isNullOrEmpty()) {
                    return@filter true
                }

                query.userQuery.durationRanges.forEach { durationRange ->
                    val minSeconds = durationRange.min.seconds
                    val maxSeconds = durationRange.max?.seconds ?: Long.MAX_VALUE
                    if ((minSeconds..maxSeconds).contains(entry.value.durationSeconds)) {
                        return@filter true
                    }
                }

                return@filter false
            }
            .filter { entry ->
                if (query.userQuery.ageRangeMin == null && query.userQuery.ageRangeMax == null) {
                    return@filter true
                }

                return@filter (entry.value.ageRangeMin == query.userQuery.ageRangeMin && entry.value.ageRangeMax == query.userQuery.ageRangeMax)
            }
            .filter { entry ->
                if (query.userQuery.ageRanges.isNullOrEmpty()) {
                    return@filter true
                }

                return@filter query.userQuery.ageRanges.any { ageRange ->
                    AgeRange(entry.value.ageRangeMin, entry.value.ageRangeMax) == ageRange
                }
            }
            .filter { entry ->
                query.userQuery.source?.let { it == entry.value.source } ?: true
            }.filter { entry ->
                (releaseDateFrom.toEpochDay()..releaseDateTo.toEpochDay()).contains(entry.value.releaseDate.toEpochDay())
            }.filter { entry ->
                checkFilterValues(query.userQuery.subjectIds, entry.value.subjects.items.map { it.id }.toSet())
            }.filter { entry ->
                query.userQuery.subjectsSetManually?.let { entry.value.subjects.setManually == it } ?: true
            }.filter { entry ->
                query.userQuery.promoted?.let { entry.value.promoted == it } ?: true
            }.filter { entry ->
                query.userQuery.active?.let { entry.value.deactivated != it } ?: true
            }
            .filter { entry ->
                if (query.userQuery.channelIds.isNotEmpty())
                    query.userQuery.channelIds.contains(entry.value.contentPartnerId)
                else true
            }
            .filter { entry ->
                query.videoAccessRuleQuery.isEligibleForStream?.let { entry.value.eligibleForStream == it } ?: true
            }
            .filter { entry ->
                val (organisationId, queriedPrices) = query.userQuery.organisationPriceFilter
                if (queriedPrices.isEmpty()) {
                    true
                } else {
                    val priceForOrganisation = entry.value.prices?.get(organisationId)?.movePointLeft(2)

                    val defaultPrice = entry.value.prices?.get("DEFAULT")?.movePointLeft(2)
                    val defaultPriceMatches = defaultPrice?.let { queriedPrices.contains(it) } ?: false

                    priceForOrganisation
                        ?.let { queriedPrices.contains(it) }
                        ?: defaultPriceMatches
                }
            }
            .filter { entry ->
                query.videoAccessRuleQuery.isEligibleForDownload?.let { entry.value.eligibleForDownload == it } ?: true
            }.filter { entry ->
                checkFilterValues(query.userQuery.attachmentTypes, entry.value.attachmentTypes)
            }
            .filter { entry ->
                if (query.videoAccessRuleQuery.includedVoiceType.isEmpty()) {
                    true
                } else {
                    query.videoAccessRuleQuery.includedVoiceType.any { voiceType ->
                        when (voiceType) {
                            VoiceType.UNKNOWN -> entry.value.isVoiced == null
                            VoiceType.WITH -> entry.value.isVoiced == true
                            VoiceType.WITHOUT -> entry.value.isVoiced == false
                        }
                    }
                }
            }
            .filter { entry -> !query.videoAccessRuleQuery.excludedLanguages.contains(entry.value.language) }
            .filter { entry -> !query.videoAccessRuleQuery.excludedSourceTypes.contains(entry.value.source) }
            .filter { entry ->
                checkFilterValues(query.userQuery.categoryCodes, entry.value.categoryCodes?.codes)
            }
            .map { video -> video.key }
    }

    private fun <T> checkFilterValues(filters: Collection<T>, values: Collection<T>?): Boolean {
        return when {
            filters.isEmpty() -> true
            values.isNullOrEmpty() -> false
            else -> matchesAnyFilterValue(filters, values)
        }
    }

    private fun <T> matchesAnyFilterValue(filterValues: Iterable<T>, entryValues: Iterable<T>): Boolean {
        return (filterValues intersect entryValues).isNotEmpty()
    }

    private fun filterByIncludedType(
        query: VideoQuery,
        entry: Map.Entry<String, VideoMetadata>
    ) = if (query.videoAccessRuleQuery.includedTypes.isEmpty() && query.userQuery.types.isEmpty()) {
        true
    } else {
        when {
            query.videoAccessRuleQuery.includedTypes.isEmpty() -> {
                query.userQuery.types.any {
                    entry.value.types.contains(
                        it
                    )
                }
            }
            query.userQuery.types.isEmpty() -> {
                query.videoAccessRuleQuery.includedTypes.any {
                    entry.value.types.contains(
                        it
                    )
                }
            }
            else -> {
                query.videoAccessRuleQuery.includedTypes.intersect(query.userQuery.types).any {
                    entry.value.types.contains(
                        it
                    )
                }
            }
        }
    }

    private fun matchOrFilters(
        query: VideoQuery,
        entry: Map.Entry<String, VideoMetadata>
    ): Boolean {
        val noCombinedFilterSpecified =
            query.videoAccessRuleQuery.permittedVideoIds.isNullOrEmpty() && query.videoAccessRuleQuery.includedChannelIds.isEmpty()

        return if (noCombinedFilterSpecified) {
            true
        } else {
            query.videoAccessRuleQuery.includedChannelIds.contains(entry.value.contentPartnerId) ||
                query.videoAccessRuleQuery.permittedVideoIds?.contains(entry.value.id) == true
        }
    }
}
