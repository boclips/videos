package com.boclips.videos.service.domain.model

import com.boclips.search.service.domain.Query

class VideoSearchQuery(
        val text: String,
        val includeTags: List<String>,
        val excludeTags: List<String>,
        val pageSize: Int,
        val pageIndex: Int) {
    fun toSearchQuery(): Query {
        return parse(this.text).copy(includeTags = includeTags, excludeTags = excludeTags)
    }

    override fun toString(): String {
        return "Query: $text, PageIndex: $pageIndex, PageSize: $pageSize"
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
