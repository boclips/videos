package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.collection.CreateDefaultCollectionCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository

class CollectionCreationService(
    private val collectionRepository: CollectionRepository,
    private val collectionIndex: CollectionIndex,
    private val collectionRetrievalService: CollectionRetrievalService
) {
    fun create(createCollectionCommand: CreateCollectionCommand, videos: List<VideoId>, user: User): Collection? {
        val createdCollection = collectionRepository.create(createCollectionCommand)

        addVideosToCollection(videos, createdCollection, user)

        collectionIndex.upsert(sequenceOf(createdCollection))

        return collectionRetrievalService.findAnyCollection(createdCollection.id, user)
    }

    fun create(createCollectionCommand: CreateDefaultCollectionCommand): Collection? {
        return collectionRepository.create(createCollectionCommand)
    }

    private fun addVideosToCollection(
        videos: List<VideoId>,
        createdCollection: Collection,
        user: User
    ) {
        videos.forEach { videoId ->
            val addVideoCommand = CollectionUpdateCommand.AddVideoToCollection(
                collectionId = createdCollection.id,
                videoId = videoId,
                user = user
            )

            // Bulk writing these commands does not work, so don't batch
            collectionRepository.update(addVideoCommand)
        }
    }
}
