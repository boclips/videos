package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class UpdateCollection(
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?) {
        getOwnedCollectionOrThrow(collectionId, collectionService)

        val commands: List<CollectionUpdateCommand> = CollectionUpdatesConverter.convert(updateCollectionRequest)

        collectionService.update(CollectionId(collectionId), commands)
    }
}
