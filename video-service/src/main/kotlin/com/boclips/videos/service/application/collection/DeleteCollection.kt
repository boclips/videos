package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.collection.CollectionIndex

class DeleteCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionIndex: CollectionIndex,
    private val collectionRetrievalService: CollectionRetrievalService
) {
    operator fun invoke(collectionId: String, user: User) {
        collectionRetrievalService.findWritable(CollectionId(value = collectionId), user = user)
            ?: throw CollectionNotFoundException(collectionId)

        collectionRepository.delete(CollectionId(collectionId), user)

        collectionIndex.removeFromSearch(collectionId)
    }
}
