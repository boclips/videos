package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionRepository

class DeleteCollection(
    private val collectionRepository: CollectionRepository
) {
    operator fun invoke(collectionId: String) {
        collectionRepository.getOwnedCollectionOrThrow(collectionId)

        collectionRepository
            .delete(CollectionId(collectionId))
    }
}