package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.videos.api.request.collection.CollectionSortKey
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.convertAgeRange

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
    val ageRangeMax: Int? = null,
    val ageRanges: List<AgeRange>? = null,
    val promoted: Boolean? = null,
    val sort: CollectionSortKey? = null,
    val resourceTypes: Set<String>? = null
) {
    fun toSearchQuery() = CollectionQuery(
        phrase = this.text ?: "",
        subjectIds = this.subjectIds,
        visibilityForOwners = this.visibilityForOwners,
        bookmarkedBy = this.bookmarkedBy,
        permittedIds = this.permittedCollections?.map { it.value },
        sort = when (this.sort) {
            CollectionSortKey.TITLE -> Sort.ByField(
                CollectionMetadata::title,
                SortOrder.ASC
            )
            CollectionSortKey.UPDATED_AT -> Sort.ByField(
                CollectionMetadata::updatedAt, SortOrder.DESC
            )
            else -> if (this.text.isNullOrBlank()) {
                Sort.ByField(
                    CollectionMetadata::hasAttachments,
                    SortOrder.DESC
                )
            } else {
                null
            }
        },
        hasLessonPlans = this.hasLessonPlans,
        ageRangeMin = this.ageRangeMin,
        ageRangeMax = this.ageRangeMax,
        ageRanges = this.ageRanges?.map { convertAgeRange(it) },
        promoted = this.promoted,
        resourceTypes = this.resourceTypes?.mapTo(HashSet()) { AttachmentType.valueOf(it).label } ?: emptySet()
    )
    fun pageIndexUpperBound() = (this.pageIndex + 1) * this.pageSize

    override fun toString(): String {
        return "CollectionQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
