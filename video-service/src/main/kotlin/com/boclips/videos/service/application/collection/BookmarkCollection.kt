package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getReadableCollectionOrThrow
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import getCurrentUserId

class BookmarkCollection(
    private val collectionService: CollectionService,
    private val analyticsEventService: AnalyticsEventService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionService.getReadableCollectionOrThrow(collectionId)
        if (collection.isMine()) throw CollectionIllegalOperationException(
            getCurrentUserId(),
            collectionId,
            "bookmark your own collection"
        )

        collectionService.bookmark(collection.id, getCurrentUserId())
        analyticsEventService.saveBookmarkCollectionEvent(collection.id)
    }
}
