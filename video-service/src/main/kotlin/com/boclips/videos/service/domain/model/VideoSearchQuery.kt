package com.boclips.videos.service.domain.model

enum class VideoSearchQueryFilter {
    EDUCATIONAL
}

class VideoSearchQuery(val text: String, val filters: List<VideoSearchQueryFilter>, val pageIndex: Int, val pageSize: Int) {
    override fun toString(): String {
        return "Query: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
