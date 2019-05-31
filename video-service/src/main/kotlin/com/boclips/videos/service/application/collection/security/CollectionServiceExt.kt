package com.boclips.videos.service.application.collection.security

import com.boclips.videos.service.application.collection.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import getCurrentUserId

fun CollectionRepository.getOwnedCollectionOrThrow(collectionId: String) =
    getCollectionOrThrow(collectionId = collectionId, collectionRepository = this, isForReading = false)

fun CollectionRepository.getReadableCollectionOrThrow(collectionId: String) =
    getCollectionOrThrow(collectionId = collectionId, collectionRepository = this, isForReading = true)

private fun getCollectionOrThrow(
    collectionId: String,
    collectionRepository: CollectionRepository,
    isForReading: Boolean
): Collection {
    val userId = getCurrentUserId()
    val collection = collectionRepository.find(CollectionId(collectionId))
        ?: throw CollectionNotFoundException(collectionId)

    return when {
        isForReading && collection.isPublic -> collection
        collection.owner != userId -> throw CollectionAccessNotAuthorizedException(userId, collectionId)
        else -> collection
    }
}
