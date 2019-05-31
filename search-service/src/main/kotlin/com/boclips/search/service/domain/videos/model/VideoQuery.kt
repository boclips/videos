package com.boclips.search.service.domain.videos.model

import com.boclips.search.service.domain.model.SearchQuery
import com.boclips.search.service.domain.model.Sort
import java.time.Duration
import java.time.LocalDate

class VideoQuery(
    phrase: String? = null,
    sort: Sort<VideoMetadata>? = null,
    val ids: List<String> = emptyList(),
    val includeTags: List<String> = emptyList(),
    val excludeTags: List<String> = emptyList(),
    val minDuration: Duration? = null,
    val maxDuration: Duration? = null,
    val source: SourceType? = null,
    val releaseDateFrom: LocalDate? = null,
    val releaseDateTo: LocalDate? = null
) : SearchQuery<VideoMetadata>(phrase, sort)
