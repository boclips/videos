package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getReadableCollectionOrThrow
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import getCurrentUserId

class UnbookmarkCollection(
    private val collectionService: CollectionService,
    private val analyticsEventService: AnalyticsEventService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionService.getReadableCollectionOrThrow(collectionId)
        if (collection.isMine()) throw CollectionIllegalOperationException(
            getCurrentUserId(),
            collectionId,
            "unbookmark your own collection"
        )

        collectionService.unbookmark(collection.id, getCurrentUserId())
        analyticsEventService.saveUnbookmarkCollectionEvent(collection.id)
    }
}
