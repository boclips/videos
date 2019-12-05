package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class BookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionAccessService: CollectionAccessService,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(collectionId: String) {
        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        val currentUser = getCurrentUser()
        if (!collectionAccessService.hasReadAccess(collection, currentUser)) {
            throw CollectionAccessNotAuthorizedException(getCurrentUserId(), collectionId)
        }

        if (collection.owner == UserId(value = currentUser.id)) throw CollectionIllegalOperationException(
            getCurrentUserId(),
            collectionId,
            "bookmark your own collection"
        )

        val result = collectionRepository.update(CollectionUpdateCommand.Bookmark(collection.id, getCurrentUserId()))

        collectionSearchService.upsert(result.map { it.collection }.asSequence())
    }
}
