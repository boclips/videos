package com.boclips.search.service.domain.common

class Counts(
    val hits: Long,
    val buckets: FilterCounts? = null)

class FilterCounts(val subjects: List<Count>)

data class Count(val id: String, val hits: Long)
