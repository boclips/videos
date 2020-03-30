package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.common.model.FacetDefinition
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.service.video.VideoAccessRuleConverter
import java.time.LocalDate

enum class SortKey {
    RELEASE_DATE,
    RATING,
    RANDOM
}

class VideoRequest(
    val text: String,
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
    val contentPartnerNames: Set<String> = emptySet(),
    val type: Set<VideoType> = emptySet(),
    val facets: VideoFacets = VideoFacets()
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
                SortKey.RANDOM -> Sort.ByRandom<VideoMetadata>()
            }
        }

        return parseIdsOrPhrase(this.text).let {
            VideoQuery(
                ids = it.ids,
                phrase = it.phrase,
                bestFor = bestFor,
                durationRanges = durationRanges,
                source = source,
                sort = sort,
                releaseDateFrom = releaseDateFrom,
                releaseDateTo = releaseDateTo,
                ageRangeMin = ageRangeMin,
                ageRangeMax = ageRangeMax,
                ageRanges = ageRanges?.map { ageRange ->
                    com.boclips.search.service.domain.videos.model.AgeRange(
                        ageRange.min(),
                        ageRange.max()
                    )
                },
                userSubjectIds = userSubjectIds,
                subjectIds = subjectsRequest.ids,
                subjectsSetManually = subjectsRequest.setManually,
                promoted = promoted,
                contentPartnerNames = contentPartnerNames,
                includedType = type,
                excludedType = VideoAccessRuleConverter.mapToExcludedVideoTypes(videoAccess),
                facetDefinition = FacetDefinition.Video(facets.ageRanges.map { ageRange ->
                    com.boclips.search.service.domain.videos.model.AgeRange(
                        ageRange.min(),
                        ageRange.max()
                    )
                }),
                permittedVideoIds = VideoAccessRuleConverter.mapToPermittedVideoIds(videoAccess),
                deniedVideoIds = VideoAccessRuleConverter.mapToDeniedVideoIds(videoAccess),
                excludedContentPartnerIds = VideoAccessRuleConverter.mapToExcludedContentPartnerIds(videoAccess),
                isEligibleForStream = VideoAccessRuleConverter.isEligibleForStreaming(videoAccess)
            )
        }
    }

    override fun toString(): String {
        return "Video Request: $text, PageIndex: $pageIndex, PageSize: $pageSize, Sort: $sortBy"
    }

    // TODO: this points to e modelling smell; perhaps we should model id query as a different type?
    private fun parseIdsOrPhrase(query: String): VideoQuery {
        val idQueryRegex = "id:(\\S+)".toRegex()
        val match = idQueryRegex.matchEntire(query)
        if (match != null) {
            val ids = match.groupValues[1].split(',')
            return VideoQuery(ids = ids)
        }
        return VideoQuery(phrase = query)
    }
}
