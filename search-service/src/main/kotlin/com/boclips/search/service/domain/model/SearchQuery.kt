package com.boclips.search.service.domain.model

abstract class SearchQuery<METADATA>(
    val phrase: String? = null,
    val sort: Sort<METADATA>? = null
)