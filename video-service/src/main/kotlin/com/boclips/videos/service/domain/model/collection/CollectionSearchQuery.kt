package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder

class CollectionSearchQuery(
    val text: String?,
    val subjectIds: List<String>,
    val visibilityForOwners: Set<VisibilityForOwner>,
    val bookmarkedBy: String? = null,
    val pageSize: Int,
    val pageIndex: Int,
    val permittedCollections: List<CollectionId>?,
    val hasLessonPlans: Boolean?,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null
) {
    fun toSearchQuery() = CollectionQuery(
        phrase = this.text ?: "",
        subjectIds = this.subjectIds,
        visibilityForOwners = this.visibilityForOwners,
        bookmarkedBy = this.bookmarkedBy,
        permittedIds = this.permittedCollections?.map { it.value },
        sort = when {
            this.text.isNullOrBlank() -> Sort.ByField(
                CollectionMetadata::hasAttachments,
                SortOrder.DESC
            )
            else -> null
        },
        hasLessonPlans = this.hasLessonPlans,
        ageRangeMin = this.ageRangeMin,
        ageRangeMax = this.ageRangeMax
    )

    fun pageIndexUpperBound() = (this.pageIndex + 1) * this.pageSize

    override fun toString(): String {
        return "CollectionQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
