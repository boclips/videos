package com.boclips.search.service.domain

class PaginatedSearchRequest<T>(
    val query: T,
    val startIndex: Int = 0,
    val windowSize: Int = 10
)
