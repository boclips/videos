package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService

class GetCollections(
    private val collectionRetrievalService: CollectionRetrievalService,
    private val collectionSearchQueryAssembler: CollectionSearchQueryAssembler
) {
    operator fun invoke(
        collectionFilterRequest: CollectionFilterRequest,
        user: User
    ): ResultsPage<Collection, Nothing> {
        val isDiscoverable = if (collectionFilterRequest.ignore_discoverable != null) {
            if (collectionFilterRequest.ignore_discoverable == true && user.isPermittedToModifyAnyCollection) {
                null
            } else {
                collectionFilterRequest.discoverable ?: true
            }
        } else {
            collectionFilterRequest.discoverable ?: true
        }

        val assembledQuery = collectionSearchQueryAssembler(
            query = collectionFilterRequest.query,
            subjects = collectionFilterRequest.subject?.split(",")?.toList() ?: emptyList(),
            bookmarked = collectionFilterRequest.bookmarked ?: false,
            owner = collectionFilterRequest.owner,
            page = collectionFilterRequest.page,
            size = collectionFilterRequest.size,
            sort = collectionFilterRequest.getSortKeys(),
            hasLessonPlans = collectionFilterRequest.has_lesson_plans,
            promoted = collectionFilterRequest.promoted,
            discoverable = isDiscoverable,
            user = user,
            ageRangeMin = collectionFilterRequest.age_range_min,
            ageRangeMax = collectionFilterRequest.age_range_max,
            ageRange = collectionFilterRequest.getAgeRanges(),
            resourceTypes = collectionFilterRequest.resource_types
        )

        return collectionRetrievalService.search(assembledQuery, user)
    }
}
