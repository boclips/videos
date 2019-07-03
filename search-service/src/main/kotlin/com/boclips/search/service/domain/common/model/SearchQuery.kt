package com.boclips.search.service.domain.common.model

abstract class SearchQuery<METADATA>(
    val phrase: String = "",
    val sort: Sort<METADATA>? = null
)