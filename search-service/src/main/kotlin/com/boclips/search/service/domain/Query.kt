package com.boclips.search.service.domain

import java.time.Duration
import kotlin.reflect.KProperty1

data class Sort(
    val fieldName: KProperty1<VideoMetadata, Comparable<*>>,
    val order: SortOrder
)

enum class SortOrder {
    ASC,
    DESC
}

data class Query constructor(
    val phrase: String? = null,
    val ids: List<String> = emptyList(),
    val sort: Sort? = null,
    val includeTags: List<String> = emptyList(),
    val excludeTags: List<String> = emptyList(),
    val minDuration: Duration? = null,
    val maxDuration: Duration? = null,
    val source: SourceType? = null
)
