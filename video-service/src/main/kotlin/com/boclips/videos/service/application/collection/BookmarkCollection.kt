package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.collection.CollectionIndex

class BookmarkCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionIndex: CollectionIndex,
    private val collectionRetrievalService: CollectionRetrievalService
) {
    operator fun invoke(collectionId: String, user: User) {
        val collection = collectionRetrievalService.find(CollectionId(value = collectionId), user = user).collection
            ?: throw CollectionNotFoundException(collectionId)

        if (collection.owner == user.id) throw CollectionIllegalOperationException(
            user.id,
            collectionId,
            "bookmark your own collection"
        )

        val result = collectionRepository.update(CollectionUpdateCommand.Bookmark(collection.id, user))

        collectionIndex.upsert(result.map { it.collection }.asSequence())
    }
}
