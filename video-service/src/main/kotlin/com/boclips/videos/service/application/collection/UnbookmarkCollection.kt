package com.boclips.videos.service.application.collection

import com.boclips.security.utils.User
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class UnbookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionAccessService: CollectionAccessService,
    private val collectionSearchService: CollectionSearchService
) {
    operator fun invoke(collectionId: String, requester: User) {
        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        val userId = UserId(value = requester.id)

        if (!collectionAccessService.hasReadAccess(collection, requester)) {
            throw CollectionAccessNotAuthorizedException(userId, collectionId)
        }

        if (collection.isOwner(userId)) throw CollectionIllegalOperationException(
            userId,
            collectionId,
            "unbookmark your own collection"
        )

        val result = collectionRepository.update(CollectionUpdateCommand.Unbookmark(collection.id, requester))

        collectionSearchService.upsert(result.map { it.collection }.asSequence())
    }
}
