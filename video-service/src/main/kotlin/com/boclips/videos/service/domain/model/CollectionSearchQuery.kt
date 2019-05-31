package com.boclips.videos.service.domain.model

import com.boclips.search.service.domain.collections.model.CollectionQuery

class CollectionSearchQuery(
    val text: String,
    val pageSize: Int,
    val pageIndex: Int
) {
    fun toSearchQuery() = CollectionQuery(phrase = this.text)

    override fun toString(): String {
        return "CollectionQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
