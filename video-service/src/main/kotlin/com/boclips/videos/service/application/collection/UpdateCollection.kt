package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionReadService
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest

class UpdateCollection(
    private val collectionSearchService: CollectionSearchService,
    private val collectionRepository: CollectionRepository,
    private val collectionUpdatesConverter: CollectionUpdatesConverter,
    private val collectionReadService: CollectionReadService
) {
    operator fun invoke(collectionId: String, updateCollectionRequest: UpdateCollectionRequest?, requester: User) {
        val collection = collectionReadService.findWritable(CollectionId(value = collectionId), user = requester)
            ?: throw CollectionNotFoundException(collectionId)

        val commands = collectionUpdatesConverter.convert(collection.id, updateCollectionRequest, requester)

        collectionRepository.update(*commands)

        collectionRepository.find(collection.id)?.let { updatedCollection ->
            collectionSearchService.upsert(sequenceOf(updatedCollection))
        }
    }
}
