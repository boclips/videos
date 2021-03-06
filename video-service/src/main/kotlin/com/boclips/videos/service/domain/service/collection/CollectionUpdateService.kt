package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import mu.KLogging

class CollectionUpdateService(
    private val collectionRepository: CollectionRepository,
    private val collectionRetrievalService: CollectionRetrievalService,
    private val collectionIndex: CollectionIndex
) {
    companion object : KLogging()

    fun addVideoToCollectionOfUser(collectionId: CollectionId, videoId: VideoId, user: User) {
        throwIfNotOwner(collectionId, user)

        collectionRetrievalService.findOwnCollection(id = collectionId, user = user)
            ?: throw CollectionIllegalOperationException(
                userId = user.idOrThrow(),
                collectionId = collectionId.value,
                operation = "Add video to collection"
            )

        val updatedCollection = collectionRepository.update(
            CollectionUpdateCommand.AddVideoToCollection(
                collectionId = collectionId,
                videoId = videoId,
                user = user
            )
        )

        collectionIndex.upsert(sequenceOf(updatedCollection))
    }

    fun removeVideoToCollectionOfUser(collectionId: CollectionId, videoId: VideoId, user: User) {
        throwIfNotOwner(collectionId, user)

        val updatedCollection = collectionRepository.update(
            CollectionUpdateCommand.RemoveVideoFromCollection(
                collectionId = collectionId,
                videoId = videoId,
                user = user
            )
        )

        collectionIndex.upsert(sequenceOf(updatedCollection))
    }

    fun updateCollectionAsOwner(updates: List<CollectionUpdateCommand>) {
        if (updates.isEmpty()) {
            return
        }

        updates.forEach { update ->
            throwIfNotOwner(update.collectionId, update.user)
        }

        val update = collectionRepository.update(updates)

        update.map { result -> result.collection }.apply {
            collectionIndex.upsert(this.asSequence())
        }
    }

    private fun throwIfNotOwner(
        collectionToBeUpdated: CollectionId,
        user: User
    ) {
        (
            collectionRetrievalService.findOwnCollection(
                id = collectionToBeUpdated,
                user = user
            )
                ?: throw CollectionIllegalOperationException(
                    userId = user.id ?: UserId("anonymous"),
                    collectionId = collectionToBeUpdated.value,
                    operation = "Update collection"
                )
            )
    }
}
