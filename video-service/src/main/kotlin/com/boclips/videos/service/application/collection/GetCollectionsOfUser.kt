package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.request.collection.CollectionSortKey
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.presentation.converters.CollectionFilterToQueryParamsConverter

class GetCollectionsOfUser(
    private val collectionRetrievalService: CollectionRetrievalService,
    private val collectionSearchQueryAssembler: CollectionSearchQueryAssembler
) {
    operator fun invoke(
        owner: UserId,
        request: CollectionFilterRequest,
        requester: User
    ): ResultsPage<Collection, Nothing> {
        val assembledQuery = collectionSearchQueryAssembler(
            query = request.query,
            subjects = request.subject?.split(",")?.toList() ?: emptyList(),
            bookmarked = request.bookmarked ?: true,
            owner = owner.value,
            page = request.page,
            size = request.size,
            sort = request.getSortKeys().ifEmpty { listOf(CollectionSortKey.UPDATED_AT) },
            hasLessonPlans = request.has_lesson_plans,
            promoted = request.promoted,
            discoverable = request.discoverable,
            user = requester,
            ageRangeMin = request.age_range_min,
            ageRangeMax = request.age_range_max,
            ageRange = request.getAgeRanges(),
            resourceTypes = request.getResourceTypes()
        )

        return collectionRetrievalService.search(
            query = assembledQuery,
            queryParams = CollectionFilterToQueryParamsConverter.convert(request),
            user = requester
        )
    }
}
