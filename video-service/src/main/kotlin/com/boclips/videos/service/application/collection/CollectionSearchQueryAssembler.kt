package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.collection.CollectionSortKey
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.converters.convertAgeRanges
import mu.KLogging

class CollectionSearchQueryAssembler {
    operator fun invoke(
        query: String? = null,
        subjects: List<String>? = null,
        promoted: Boolean? = null,
        curated: Boolean? = null,
        bookmarked: Boolean? = null,
        owner: String? = null,
        page: Int? = null,
        size: Int? = null,
        sort: CollectionSortKey? = null,
        hasLessonPlans: Boolean? = null,
        user: User,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        ageRange: List<String>? = null,
        resourceTypes: Set<String>? = null
    ): CollectionSearchQuery {
        val collectionAccess = user.accessRules.collectionAccess

        val bookmarker =
            if (bookmarked == true)
                when (collectionAccess) {
                    is CollectionAccessRule.SpecificOwner -> collectionAccess.owner
                    is CollectionAccessRule.Everything -> user.id
                    else -> throw OperationForbiddenException(
                        "This user cannot have bookmarked collections"
                    )
                } else null

        bookmarker?.let { logger.info("Looking for collections bookmarked by $it") }
        logger.info { "User has collection access: $collectionAccess" }

        return CollectionSearchQuery(
            text = query ?: "",
            subjectIds = subjects ?: emptyList(),
            bookmarkedBy = bookmarker?.value,
            curated = curated,
            promoted = promoted,
            hasLessonPlans = hasLessonPlans,
            owner = when (owner) {
                null -> when (collectionAccess) {
                    is CollectionAccessRule.SpecificOwner -> collectionAccess.owner.value
                    is CollectionAccessRule.SpecificIds -> null
                    CollectionAccessRule.Everything -> null
                }
                else -> owner
            },
            permittedCollections = when (collectionAccess) {
                is CollectionAccessRule.SpecificIds -> collectionAccess.collectionIds.toList()
                else -> null
            },
            pageSize = size ?: CollectionsController.COLLECTIONS_PAGE_SIZE,
            pageIndex = page ?: 0,
            sort = sort,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            ageRanges = ageRange?.map(::convertAgeRanges),
            resourceTypes = resourceTypes
        )
    }

    companion object : KLogging()
}
