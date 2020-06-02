package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.videos.api.request.collection.CollectionSortKey
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.convertAgeRange

class CollectionSearchQuery(
    val text: String?,
    val subjectIds: List<String>,
    val owner: String? = null,
    val bookmarkedBy: String? = null,
    val pageSize: Int,
    val pageIndex: Int,
    val permittedCollections: List<CollectionId>?,
    val hasLessonPlans: Boolean?,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val ageRanges: List<AgeRange>? = null,
    val promoted: Boolean? = null,
    val discoverable: Boolean? = null,
    val sort: List<CollectionSortKey> = emptyList(),
    val resourceTypes: Set<String>? = null
) {
    fun toSearchQuery(): CollectionQuery {
        return CollectionQuery(
            phrase = this.text ?: "",
            subjectIds = this.subjectIds,
            searchable = this.discoverable,
            owner = this.owner,
            bookmarkedBy = this.bookmarkedBy,
            permittedIds = this.permittedCollections?.map { it.value },
            sort = sort.mapNotNull { sort ->
                return@mapNotNull when (sort) {
                    CollectionSortKey.IS_DEFAULT -> Sort.ByField(
                        CollectionMetadata::default, SortOrder.DESC
                    )
                    CollectionSortKey.HAS_ATTACHMENT -> Sort.ByField(
                        CollectionMetadata::hasAttachments, SortOrder.DESC
                    )
                    CollectionSortKey.TITLE -> Sort.ByField(
                        CollectionMetadata::title,
                        SortOrder.ASC
                    )
                    CollectionSortKey.UPDATED_AT -> Sort.ByField(
                        CollectionMetadata::updatedAt, SortOrder.DESC
                    )
                    else -> null
                }
            },
            hasLessonPlans = this.hasLessonPlans,
            ageRangeMin = this.ageRangeMin,
            ageRangeMax = this.ageRangeMax,
            ageRanges = this.ageRanges?.map { convertAgeRange(it) },
            promoted = this.promoted,
            resourceTypes = this.resourceTypes?.mapTo(HashSet()) { AttachmentType.valueOf(it).label } ?: emptySet()
        )
    }

    fun pageIndexUpperBound() = (this.pageIndex + 1) * this.pageSize

    override fun toString(): String {
        return "CollectionQuery: $text, PageIndex: $pageIndex, PageSize: $pageSize"
    }
}
