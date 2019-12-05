package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class AddVideoToCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionAccessService: CollectionAccessService
) {
    operator fun invoke(collectionId: String?, videoId: String?) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        if (!collectionAccessService.hasWriteAccess(collectionId)) {
            throw CollectionAccessNotAuthorizedException(getCurrentUserId(), collectionId)
        }

        collectionRepository.update(
            CollectionUpdateCommand.AddVideoToCollection(
                collectionId = CollectionId(collectionId),
                videoId = VideoId(videoId)
            )
        )
    }
}
