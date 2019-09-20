package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.videos.service.application.collection.CollectionFilter

class CollectionSearchQuery(
    val text: String?,
    val subjectIds: List<String>,
    val visibility: List<CollectionVisibility>,
    val pageSize: Int,
    val pageIndex: Int
) {
    fun toSearchQuery() = CollectionQuery(
        phrase = this.text ?: "",
        subjectIds = this.subjectIds,
        visibility = visibility,
        sort = when {
            this.subjectIds.isNotEmpty() && this.text.isNullOrBlank() -> Sort(
                CollectionMetadata::hasAttachments,
                SortOrder.DESC
            )
            else -> null
        }
    )

    fun pageIndexUpperBound() = (this.pageIndex + 1) * this.pageSize

    override fun toString(): String {
        return "CollectionQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
