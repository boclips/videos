package com.boclips.search.service.domain

data class Query(
        val phrase: String? = null,
        val ids: List<String> = emptyList(),
        val includeTags: List<String> = emptyList(),
        val excludeTags: List<String> = emptyList()
)