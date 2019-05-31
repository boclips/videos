package com.boclips.search.service.domain.model

class PaginatedSearchRequest<T>(
    val query: T,
    val startIndex: Int = 0,
    val windowSize: Int = 10
)
