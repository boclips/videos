package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.convertAgeRange
import com.boclips.videos.service.domain.model.video.VideoAccess
import java.time.LocalDate

enum class SortKey {
    RELEASE_DATE,
    RATING,
    TITLE_DESC,
    TITLE_ASC,
    INGEST_ASC,
    INGEST_DESC,
    RANDOM
}

class VideoRequest(
    val text: String,
    val ids: Set<String> = emptySet(),
    val sortBy: SortKey? = null,
    val bestFor: List<String>? = null,
    val durationRanges: List<DurationRange>? = null,
    val releaseDateFrom: LocalDate? = null,
    val releaseDateTo: LocalDate? = null,
    val pageSize: Int,
    val pageIndex: Int,
    val source: SourceType? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val ageRanges: List<AgeRange>? = null,
    val userSubjectIds: Set<String> = emptySet(),
    val subjectsRequest: SubjectsRequest = SubjectsRequest(),
    val promoted: Boolean? = null,
    val channelNames: Set<String> = emptySet(),
    val channelIds: Set<String>? = emptySet(),
    val types: Set<VideoType> = emptySet(),
    val facets: VideoFacets = VideoFacets(),
    val attachmentTypes: Set<String> = emptySet()
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
            }
        }

        return VideoQuery(
            phrase = text,
            videoSort = sort,
            facetDefinition = FacetDefinition.Video(
                ageRangeBuckets = facets.ageRanges.map { ageRange -> convertAgeRange(ageRange) },
                duration = facets.durations.map { duration -> DurationRange(duration.first, duration.second) },
                resourceTypes = facets.attachmentTypes,
                includeChannelFacets = facets.includeChannelFacets
            ),
            accessRuleQuery = AccessRuleQueryConverter.fromAccessRules(videoAccess),
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
                promoted = promoted,
                active = true,
                channelNames = channelNames,
                channelIds = channelIds,
                types = types,
                attachmentTypes = attachmentTypes
            )
        )
    }

    override fun toString(): String {
        return "Video Request: $text, PageIndex: $pageIndex, PageSize: $pageSize, Sort: $sortBy"
    }
}
