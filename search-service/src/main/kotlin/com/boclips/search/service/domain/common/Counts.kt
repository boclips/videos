package com.boclips.search.service.domain.common

class Counts(
    val hits: Long,
    val filters: FilterCounts? = null)

class FilterCounts(val subjects: List<Count>)

class Count(val id: String, val total: Long)
