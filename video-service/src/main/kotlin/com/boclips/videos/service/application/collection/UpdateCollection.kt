package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class UpdateCollection(
    private val collectionSearchService: CollectionSearchService,
    private val collectionRepository: CollectionRepository,
    private val collectionUpdatesConverter: CollectionUpdatesConverter,
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?) {
        collectionService.getOwnedCollectionOrThrow(collectionId)
        val id = CollectionId(collectionId)

        val commands = collectionUpdatesConverter.convert(id, updateCollectionRequest)

        collectionRepository.bulkUpdate(commands)

        collectionRepository.find(id)?.let { collection ->
            collectionSearchService.upsert(sequenceOf(collection))
        }
    }
}
