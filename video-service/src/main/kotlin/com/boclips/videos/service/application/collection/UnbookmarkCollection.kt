package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.CollectionReadService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService

class UnbookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService,
    private val collectionReadService: CollectionReadService
) {
    operator fun invoke(collectionId: String, user: User) {
        val collection = collectionReadService.find(CollectionId(value = collectionId), user = user).collection
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
