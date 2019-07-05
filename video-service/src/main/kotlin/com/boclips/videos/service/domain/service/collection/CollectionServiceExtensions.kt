package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository

fun CollectionRepository.getOwnedCollectionOrThrow(collectionId: String) =
    getCollectionOrThrow(
        collectionId = collectionId,
        collectionRepository = this,
        isForReading = false
    )

fun CollectionRepository.getReadableCollectionOrThrow(collectionId: String) =
    getCollectionOrThrow(
        collectionId = collectionId,
        collectionRepository = this,
        isForReading = true
    )

private fun getCollectionOrThrow(
    collectionId: String,
    collectionRepository: CollectionRepository,
    isForReading: Boolean
): Collection {
    val userId = getCurrentUserId()
    val collection = collectionRepository.find(
        CollectionId(
            collectionId
        )
    )
        ?: throw CollectionNotFoundException(collectionId)

    return when {
        isForReading && collection.isPublic -> collection
        collection.owner == userId -> collection
        collection.viewerIds.contains(userId) -> collection
        else -> throw CollectionAccessNotAuthorizedException(
            userId,
            collectionId
        )
    }
}
