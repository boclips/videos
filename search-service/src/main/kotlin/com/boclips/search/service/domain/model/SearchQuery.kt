package com.boclips.search.service.domain.model

abstract class SearchQuery<METADATA>(
    val phrase: String = "",
    val sort: Sort<METADATA>? = null
)