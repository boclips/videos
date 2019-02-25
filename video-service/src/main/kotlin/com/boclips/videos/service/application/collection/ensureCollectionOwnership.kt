package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import getCurrentUserId

fun getOwnedCollectionOrThrow(collectionId: String, collectionService: CollectionService): Collection {
    val userId = UserExtractor.getCurrentUserId()
    val collection = collectionService.getById(CollectionId(collectionId))

    if (collection == null) {
        throw CollectionNotFoundException(collectionId)
    } else if (collection.owner != userId) {
        throw CollectionAccessNotAuthorizedException(userId, collectionId)
    }
    return collection
}