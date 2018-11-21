package com.boclips.search.service.domain

class PaginatedSearchRequest(
        val query: String,
        val pageIndex: Int = 0,
        val pageSize: Int = 100
)