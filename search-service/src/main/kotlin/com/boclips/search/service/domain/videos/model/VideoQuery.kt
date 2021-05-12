package com.boclips.search.service.domain.videos.model

import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort
import java.time.LocalDate
import java.util.Locale

data class VideoQuery(
    override val phrase: String = "",
    override val facetDefinition: FacetDefinition.Video? = null,
    val videoSort: Sort<VideoMetadata>? = null,
    val userQuery: UserQuery = UserQuery(),
    val videoAccessRuleQuery: VideoAccessRuleQuery
) : SearchQuery<VideoMetadata>(phrase, videoSort?.let { listOf(it) } ?: emptyList(), facetDefinition)

data class UserQuery(
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
    val categoryCodes: Set<String> = emptySet(),
    val promoted: Boolean? = null,
    val active: Boolean? = null,
    val types: Set<VideoType> = emptySet(),
    val attachmentTypes: Set<String> = emptySet(),
    val channelIds: Set<String> = emptySet(),
    val organisationPriceFilter: PricesFilter = PricesFilter(),
)

data class VideoAccessRuleQuery(
    val excludedContentPartnerIds: Set<String> = emptySet(),
    val includedChannelIds: Set<String> = emptySet(),
    val includedTypes: Set<VideoType> = emptySet(),
    val excludedTypes: Set<VideoType> = emptySet(),
    val permittedVideoIds: Set<String>? = null,
    val deniedVideoIds: Set<String>? = null,
    val isEligibleForStream: Boolean? = null,
    val isEligibleForDownload: Boolean? = null,
    val includedVoiceType: Set<VoiceType> = emptySet(),
    val excludedLanguages: Set<Locale> = emptySet()

)
