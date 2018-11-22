package com.boclips.videos.service.domain.model

class VideoSearchQuery(val text: String, val pageIndex: Int, val pageSize: Int) {
    override fun toString(): String {
        return "Query: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
