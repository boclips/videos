package com.boclips.search.service.domain

data class Query(
        val phrase: String? = null,
        val ids: List<String> = emptyList(),
        val filters: List<Filter> = emptyList()
)