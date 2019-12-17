package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class UnbookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String, user: User) {
        val collection = collectionService.find(CollectionId(value = collectionId), user = user)
            ?: throw CollectionNotFoundException(collectionId)

        if (collection.isOwner(user)) throw CollectionIllegalOperationException(
            user.id,
            collectionId,
            "unbookmark your own collection"
        )

        val result = collectionRepository.update(CollectionUpdateCommand.Unbookmark(collection.id, user))

        collectionSearchService.upsert(result.map { it.collection }.asSequence())
    }
}
