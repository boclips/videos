package com.boclips.videos.service.domain.model.video

import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.AgeRange
import java.time.LocalDate

enum class SortKey {
    RELEASE_DATE,
    RATING,
    RANDOM
}

data class SubjectQuery(
    val ids: Set<String> = emptySet(),
    val setManually: Boolean? = null
)

class VideoIdsQuery(
    val ids: List<VideoId>
) {
    fun toSearchQuery(videoAccess: VideoAccess): VideoQuery {
        return VideoQuery(
            ids = ids.map { it.value },
            permittedVideoIds = getPermittedVideoIds(videoAccess)
        )
    }
}

class VideoSearchQuery(
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
    val subjectQuery: SubjectQuery = SubjectQuery(),
    val promoted: Boolean? = null,
    val contentPartnerNames: Set<String> = emptySet(),
    val type: Set<VideoType> = emptySet(),
    val isClassroom: Boolean? = null
) {
    fun toSearchQuery(videoAccess: VideoAccess): VideoQuery {
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
                subjectIds = subjectQuery.ids,
                subjectsSetManually = subjectQuery.setManually,
                promoted = promoted,
                contentPartnerNames = contentPartnerNames,
                type = type,
                isClassroom = isClassroom,
                permittedVideoIds = getPermittedVideoIds(videoAccess)
            )
        }
    }

    override fun toString(): String {
        return "VideoQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize, Sort: $sortBy"
    }

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

fun getPermittedVideoIds(videoAccess: VideoAccess): Set<String>? {
    return when (videoAccess) {
        is VideoAccess.Rules -> videoAccess.accessRules.flatMap { accessRule ->
            when (accessRule) {
                is VideoAccessRule.SpecificIds -> accessRule.videoIds.map { id -> id.value }
            }
        }.toSet()
        VideoAccess.Everything -> null
    }
}