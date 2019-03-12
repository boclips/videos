package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService

class DeleteCollection(
        private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String) {
        collectionService.getOwnedCollectionOrThrow(collectionId)

        collectionService
                .delete(CollectionId(collectionId))
    }
}