package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getReadableCollectionOrThrow
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import getCurrentUserId

class UnbookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val analyticsEventService: AnalyticsEventService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionRepository.getReadableCollectionOrThrow(collectionId)
        if (collection.isMine()) throw CollectionIllegalOperationException(
            getCurrentUserId(),
            collectionId,
            "unbookmark your own collection"
        )

        collectionRepository.unbookmark(collection.id, getCurrentUserId())
        analyticsEventService.saveUnbookmarkCollectionEvent(collection.id)
    }
}
