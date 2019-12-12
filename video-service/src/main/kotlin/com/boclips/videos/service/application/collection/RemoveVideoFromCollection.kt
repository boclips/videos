package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class RemoveVideoFromCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionAccessService: CollectionAccessService
) {
    operator fun invoke(collectionId: String?, videoId: String?, user: User) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        if (!collectionAccessService.hasWriteAccess(collection, user)) {
            throw CollectionAccessNotAuthorizedException(user.id, collectionId)
        }

        collectionRepository.update(
            CollectionUpdateCommand.RemoveVideoFromCollection(
                collectionId = CollectionId(collectionId),
                videoId = VideoId(videoId),
                user = user
            )
        )
    }
}
