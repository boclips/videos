package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService

class DeleteCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String) {
        collectionService.getOwnedCollectionOrThrow(collectionId)

        collectionRepository.delete(CollectionId(collectionId))

        collectionSearchService.removeFromSearch(collectionId)
    }
}