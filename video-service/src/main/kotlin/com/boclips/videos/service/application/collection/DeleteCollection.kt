package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService

class DeleteCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(collectionId: String) {
        collectionRepository.getOwnedCollectionOrThrow(collectionId)

        collectionRepository.delete(CollectionId(collectionId))

        collectionSearchService.removeFromSearch(collectionId)
    }
}