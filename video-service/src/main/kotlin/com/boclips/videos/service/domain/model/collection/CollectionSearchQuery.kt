package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionQuery

class CollectionSearchQuery(
    val text: String?,
    val subjectIds: List<String>,
    val pageSize: Int,
    val pageIndex: Int
) {
    fun toSearchQuery() = CollectionQuery(phrase = this.text ?: "", subjectIds = this.subjectIds)

    fun pageIndexUpperBound() = (this.pageIndex + 1) * this.pageSize

    override fun toString(): String {
        return "CollectionQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
