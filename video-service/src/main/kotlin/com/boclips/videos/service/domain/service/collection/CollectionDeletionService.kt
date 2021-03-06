package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.infrastructure.collection.CollectionRepository

class CollectionDeletionService(
    private val collectionRepository: CollectionRepository,
    private val collectionIndex: CollectionIndex,
    private val collectionRetrievalService: CollectionRetrievalService
) {
    fun delete(collectionId: CollectionId, user: User) {
        collectionRetrievalService.findOwnCollection(collectionId, user = user)
            ?: throw CollectionNotFoundException(collectionId.value)

        collectionRepository.delete(collectionId, user)

        collectionIndex.removeFromSearch(collectionId.value)
    }
}
