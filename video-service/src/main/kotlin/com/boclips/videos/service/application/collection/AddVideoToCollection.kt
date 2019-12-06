package com.boclips.videos.service.application.collection

import com.boclips.security.utils.User
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class AddVideoToCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionAccessService: CollectionAccessService
) {
    operator fun invoke(collectionId: String?, videoId: String?, requester: User) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        val collection = collectionRepository.find(CollectionId(value = collectionId))
            ?: throw CollectionNotFoundException(collectionId)

        if (!collectionAccessService.hasWriteAccess(collection, requester)) {
            throw CollectionAccessNotAuthorizedException(UserId(requester.id), collection.id)
        }

        collectionRepository.update(
            CollectionUpdateCommand.AddVideoToCollection(
                collectionId = CollectionId(collectionId),
                videoId = VideoId(videoId),
                user = requester
            )
        )
    }
}
