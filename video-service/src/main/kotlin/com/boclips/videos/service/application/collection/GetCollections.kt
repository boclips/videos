package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.collection.CollectionReadService

class GetCollections(
    private val collectionReadService: CollectionReadService,
    private val collectionSearchQueryAssembler: CollectionSearchQueryAssembler
) {
    operator fun invoke(
        collectionFilterRequest: CollectionFilterRequest,
        user: User
    ): Page<Collection> {
        val assembledQuery = collectionSearchQueryAssembler(
            query = collectionFilterRequest.query,
            subjects = collectionFilterRequest.subject?.split(",")?.toList() ?: emptyList(),
            public = collectionFilterRequest.public,
            bookmarked = collectionFilterRequest.bookmarked ?: false,
            owner = collectionFilterRequest.owner,
            page = collectionFilterRequest.page,
            size = collectionFilterRequest.size,
            hasLessonPlans = collectionFilterRequest.has_lesson_plans,
            user = user,
            ageRangeMin = collectionFilterRequest.ageRangeMin,
            ageRangeMax = collectionFilterRequest.ageRangeMax
        )

        return collectionReadService.search(assembledQuery, user)
    }
}
