package com.boclips.search.service.domain.videos.model

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort
import java.time.Duration
import java.time.LocalDate

data class DurationRange(
    val min: Duration,
    val max: Duration? = null
)

class VideoQuery(
    phrase: String = "",
    sort: Sort<VideoMetadata>? = null,
    val ids: List<String> = emptyList(),
    val bestFor: List<String>? = null,
    val durationRanges: List<DurationRange>? = null,
    val source: SourceType? = null,
    val releaseDateFrom: LocalDate? = null,
    val releaseDateTo: LocalDate? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val userSubjectIds: Set<String> = emptySet(),
    val subjectIds: Set<String> = emptySet(),
    val subjectsSetManually: Boolean? = null,
    val promoted: Boolean? = null,
    val contentPartnerNames: Set<String> = emptySet(),
    val type: Set<VideoType> = emptySet(),
    val permittedVideoIds: Set<String>? = null,
    val deniedVideoIds: Set<String>? = null,
    val isClassroom: Boolean? = null
) : SearchQuery<VideoMetadata>(phrase, sort)
