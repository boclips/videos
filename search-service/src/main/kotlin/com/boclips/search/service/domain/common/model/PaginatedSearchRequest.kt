package com.boclips.search.service.domain.common.model

class PaginatedSearchRequest<T>(
    val query: T,
    val startIndex: Int = 0,
    val windowSize: Int = 10
)
