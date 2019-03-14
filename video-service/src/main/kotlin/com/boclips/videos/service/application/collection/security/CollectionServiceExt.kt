package com.boclips.videos.service.application.collection.security

import com.boclips.videos.service.application.collection.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import getCurrentUserId

fun CollectionService.getOwnedCollectionOrThrow(collectionId: String) =
        getCollectionOrThrow(collectionId = collectionId, collectionService = this, isForReading = false)

fun CollectionService.getReadableCollectionOrThrow(collectionId: String) =
        getCollectionOrThrow(collectionId = collectionId, collectionService = this, isForReading = true)

private fun getCollectionOrThrow(collectionId: String, collectionService: CollectionService, isForReading: Boolean): Collection {
    val userId = getCurrentUserId()
    val collection = collectionService.getById(CollectionId(collectionId))
            ?: throw CollectionNotFoundException(collectionId)

    return when {
        isForReading && collection.isPublic -> collection
        collection.owner != userId -> throw CollectionAccessNotAuthorizedException(userId, collectionId)
        else -> collection
    }
}
