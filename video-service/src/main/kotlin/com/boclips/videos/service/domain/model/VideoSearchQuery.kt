package com.boclips.videos.service.domain.model

import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.Sort
import com.boclips.search.service.domain.SortOrder
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.SortKey.RELEASE_DATE

enum class SortKey {
    RELEASE_DATE
}

class VideoSearchQuery(
    val text: String,
    val sortBy: SortKey? = null,
    val includeTags: List<String>,
    val excludeTags: List<String>,
    val pageSize: Int,
    val pageIndex: Int
) {
    fun toSearchQuery(): Query {
        val sort = sortBy?.let {
            when (it) {
                RELEASE_DATE -> Sort(order = SortOrder.DESC, fieldName = VideoMetadata::releaseDate)
            }
        }
        return parse(this.text).copy(
            includeTags = includeTags,
            excludeTags = excludeTags,
            sort = sort
        )
    }

    override fun toString(): String {
        return "Query: $text, PageIndex: $pageIndex, PageSize: $pageSize, Sort: $sortBy"
    }

    private fun parse(query: String): Query {
        val idQueryRegex = "id:(\\S+)".toRegex()
        val match = idQueryRegex.matchEntire(query)
        if (match != null) {
            val ids = match.groupValues[1].split(',')
            return Query(ids = ids)
        }
        return Query(phrase = query)
    }
}
