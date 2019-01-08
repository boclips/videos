package com.boclips.videos.service.domain.model

import com.boclips.search.service.domain.Filter
import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.VideoMetadata

class VideoSearchQuery(
        val text: String,
        val filters: List<VideoSearchQueryFilter>,
        val pageIndex: Int,
        val pageSize: Int) {
    fun toSearchQuery(): Query {
        val filters = this.filters.map {
            when (it) {
                VideoSearchQueryFilter.EDUCATIONAL -> Filter(VideoMetadata::isEducational, true)
                VideoSearchQueryFilter.NEWS -> Filter(VideoMetadata::isNews, true)
            }
        }

        return parse(this.text).copy(filters = filters)
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
