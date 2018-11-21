package com.boclips.search.service.domain

class PaginatedSearchRequest(
        val query: String,
        val startIndex: Int = 0,
        val windowSize: Int = 10
)