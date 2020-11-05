package com.boclips.search.service.domain.common.model

sealed class IndexSearchRequest<T>(
    val query: T,
    val windowSize: Int
) {
    fun isCursorBased(): Boolean =
        when (this) {
            is PaginatedIndexSearchRequest -> false
            is CursorBasedIndexSearchRequest -> true
        }
}

class PaginatedIndexSearchRequest<T>(
    query: T,
    windowSize: Int = 10,
    val startIndex: Int = 0
) : IndexSearchRequest<T>(query, windowSize)

class CursorBasedIndexSearchRequest<T>(
    query: T,
    windowSize: Int = 10000,
    val cursor: PagingCursor? = null
) : IndexSearchRequest<T>(query, windowSize)
