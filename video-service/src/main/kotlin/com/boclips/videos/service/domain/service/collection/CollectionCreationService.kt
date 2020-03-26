package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId

class CollectionCreationService(
    private val collectionRepository: CollectionRepository,
    private val collectionReadService: CollectionReadService
) {
    fun create(createCollectionCommand: CreateCollectionCommand, videos: List<VideoId>, user: User): Collection? {
        val createdCollection = collectionRepository.create(createCollectionCommand)

        addVideosToCollection(videos, createdCollection, user)

        return collectionReadService.find(createdCollection.id, user).collection
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
