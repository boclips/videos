package com.boclips.search.service.domain.videos.model

import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort
import java.time.LocalDate

data class VideoQuery(
    override val phrase: String = "",
    override val facetDefinition: FacetDefinition.Video? = null,
    val videoSort: Sort<VideoMetadata>? = null,
    val ids: Set<String> = emptySet(),
    val bestFor: List<String>? = null,
    val durationRanges: List<DurationRange>? = null,
    val source: SourceType? = null,
    val releaseDateFrom: LocalDate? = null,
    val releaseDateTo: LocalDate? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val ageRanges: List<AgeRange>? = null,
    val userSubjectIds: Set<String> = emptySet(),
    val subjectIds: Set<String> = emptySet(),
    val subjectsSetManually: Boolean? = null,
    val promoted: Boolean? = null,
    val active: Boolean? = null,
    val channelNames: Set<String> = emptySet(),
    val excludedContentPartnerIds: Set<String> = emptySet(),
    val includedChannelIds: Set<String> = emptySet(),
    val includedTypes: Set<VideoType> = emptySet(),
    val excludedTypes: Set<VideoType> = emptySet(),
    val permittedVideoIds: Set<String>? = null,
    val deniedVideoIds: Set<String>? = null,
    val isEligibleForStream: Boolean? = null,
    val attachmentTypes: Set<String> = emptySet(),
    val includedVoiceType: Set<VoiceType> = emptySet()
) : SearchQuery<VideoMetadata>(phrase, videoSort?.let { listOf(it) } ?: emptyList(), facetDefinition)
