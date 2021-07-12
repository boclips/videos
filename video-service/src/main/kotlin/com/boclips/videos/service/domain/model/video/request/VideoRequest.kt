package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.*
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.convertAgeRange
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.video.VideoAccess
import java.math.BigDecimal
import java.time.LocalDate

enum class SortKey {
    RELEASE_DATE,
    RATING,
    TITLE_DESC,
    TITLE_ASC,
    INGEST_ASC,
    INGEST_DESC,
    UNTAGGED_CATEGORIES,
    RANDOM
}

sealed class VideoRequestPagingState {
    data class Cursor(val value: String?) : VideoRequestPagingState()
    data class PageNumber(val number: Int) : VideoRequestPagingState()
}

class VideoRequest(
    val text: String,
    val pageSize: Int,
    val pagingState: VideoRequestPagingState,
    val ids: Set<String> = emptySet(),
    val sortBy: SortKey? = null,
    val bestFor: List<String>? = null,
    val durationRanges: List<DurationRange>? = null,
    val releaseDateFrom: LocalDate? = null,
    val releaseDateTo: LocalDate? = null,
    val source: SourceType? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val ageRanges: List<AgeRange>? = null,
    val userSubjectIds: Set<String> = emptySet(),
    val subjectsRequest: SubjectsRequest = SubjectsRequest(),
    val promoted: Boolean? = null,
    val channelIds: Set<String> = emptySet(),
    val types: Set<VideoType> = emptySet(),
    val facets: VideoFacets = VideoFacets(),
    val attachmentTypes: Set<String> = emptySet(),
    val userOrganisationId: OrganisationId? = null,
    val prices: Set<BigDecimal> = emptySet(),
    val categoryCodes: Set<String> = emptySet(),
    val updatedAtFrom: LocalDate? = null
) {
    fun toQuery(videoAccess: VideoAccess): VideoQuery {

        val sort = sortBy?.let {
            when (it) {
                SortKey.RELEASE_DATE -> Sort.ByField(
                    order = SortOrder.DESC,
                    fieldName = VideoMetadata::releaseDate
                )
                SortKey.RATING -> Sort.ByField(
                    order = SortOrder.DESC,
                    fieldName = VideoMetadata::meanRating
                )
                SortKey.TITLE_DESC -> Sort.ByField(
                    order = SortOrder.DESC,
                    fieldName = VideoMetadata::rawTitle
                )
                SortKey.TITLE_ASC -> Sort.ByField(
                    order = SortOrder.ASC,
                    fieldName = VideoMetadata::rawTitle
                )
                SortKey.INGEST_ASC -> Sort.ByField(
                    order = SortOrder.ASC,
                    fieldName = VideoMetadata::ingestedAt
                )
                SortKey.INGEST_DESC -> Sort.ByField(
                    order = SortOrder.DESC,
                    fieldName = VideoMetadata::ingestedAt
                )
                SortKey.RANDOM -> Sort.ByRandom<VideoMetadata>()
                SortKey.UNTAGGED_CATEGORIES -> Sort.ByField(
                    order = SortOrder.ASC,
                    fieldName = VideoMetadata::categoryCodes
                )
            }
        }

        return VideoQuery(
            phrase = text,
            videoSort = sort,
            facetDefinition = FacetDefinition.Video(
                ageRangeBuckets = facets.ageRanges.map { ageRange -> convertAgeRange(ageRange) },
                duration = facets.durations.map { duration -> DurationRange(duration.first, duration.second) },
                resourceTypes = facets.attachmentTypes,
                includeChannelFacets = facets.includeChannelFacets,
                includePriceFacets = facets.includePriceFacets,
                videoTypes = facets.videoTypes
            ),
            videoAccessRuleQuery = AccessRuleQueryConverter.toVideoAccessRuleQuery(videoAccess),
            userQuery = UserQuery(
                ids = ids,
                bestFor = bestFor,
                durationRanges = durationRanges,
                source = source,
                releaseDateFrom = releaseDateFrom,
                releaseDateTo = releaseDateTo,
                ageRangeMin = ageRangeMin,
                ageRangeMax = ageRangeMax,
                ageRanges = ageRanges?.map { ageRange -> convertAgeRange(ageRange) },
                userSubjectIds = userSubjectIds,
                subjectIds = subjectsRequest.ids,
                subjectsSetManually = subjectsRequest.setManually,
                categoryCodes = categoryCodes,
                promoted = promoted,
                active = true,
                channelIds = channelIds,
                types = types,
                attachmentTypes = attachmentTypes,
                organisationPriceFilter = PricesFilter(userOrganisationId?.value, prices),
                updatedAtFrom = updatedAtFrom
            )
        )
    }

    override fun toString(): String {
        val pagingText = when (pagingState) {
            is VideoRequestPagingState.Cursor -> {
                val cursorText = pagingState.value ?: "<first request for cursor>"
                "Cursor ID: $cursorText"
            }
            is VideoRequestPagingState.PageNumber -> "PageIndex: ${pagingState.number}"
        }
        return "Video Request: $text, $pagingText, PageSize: $pageSize, Sort: $sortBy"
    }
}
