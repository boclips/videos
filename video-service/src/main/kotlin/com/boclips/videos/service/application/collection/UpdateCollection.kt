package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class UpdateCollection(
    private val collectionRepository: CollectionRepository,
    private val analyticsEventService: AnalyticsEventService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?) {
        collectionRepository.getOwnedCollectionOrThrow(collectionId)

        val commands = CollectionUpdatesConverter.convert(updateCollectionRequest)

        collectionRepository.update(CollectionId(collectionId), commands)
        analyticsEventService.saveUpdateCollectionEvent(CollectionId(collectionId), commands)
    }
}
