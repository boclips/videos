package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import getCurrentUserId

fun getOwnedCollectionOrThrow(collectionId: String, collectionService: CollectionService) =
        getCollectionOrThrow(collectionId = collectionId, collectionService = collectionService, isForReading = false)

fun getReadableCollectionOrThrow(collectionId: String, collectionService: CollectionService) =
        getCollectionOrThrow(collectionId = collectionId, collectionService = collectionService, isForReading = true)

private fun getCollectionOrThrow(collectionId: String, collectionService: CollectionService, isForReading: Boolean): Collection {
    val userId = UserExtractor.getCurrentUserId()
    val collection = collectionService.getById(CollectionId(collectionId))
            ?: throw CollectionNotFoundException(collectionId)

    return when {
        isForReading && collection.isPublic -> collection
        collection.owner != userId -> throw CollectionAccessNotAuthorizedException(userId, collectionId)
        else -> collection
    }
}
