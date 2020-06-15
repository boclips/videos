package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository

class CollectionBookmarkService(
    private val collectionRetrievalService: CollectionRetrievalService,
    private val collectionIndex: CollectionIndex,
    private val collectionRepository: CollectionRepository
) {
    fun bookmark(collectionId: CollectionId, user: User) {
        val collection = collectionRetrievalService.findAnyCollection(collectionId, user)
            ?: throw CollectionNotFoundException(collectionId.value)

        if (collection.owner == user.id) throw CollectionIllegalOperationException(
            user.id,
            collectionId.value,
            "bookmark your own collection"
        )

        val updatedCollection = collectionRepository.update(CollectionUpdateCommand.Bookmark(collection.id, user))

        collectionIndex.upsert(sequenceOf(updatedCollection))
    }

    fun unbookmark(collectionId: CollectionId, user: User) {
        val collection = collectionRetrievalService.findAnyCollection(collectionId, user)
            ?: throw CollectionNotFoundException(collectionId.value)

        if (collection.isOwner(user)) throw CollectionIllegalOperationException(
            user.id ?: UserId("anonymous"),
            collectionId.value,
            "unbookmark your own collection"
        )

        val updatedCollection = collectionRepository.update(CollectionUpdateCommand.Unbookmark(collection.id, user))

        collectionIndex.upsert(sequenceOf(updatedCollection))
    }
}
