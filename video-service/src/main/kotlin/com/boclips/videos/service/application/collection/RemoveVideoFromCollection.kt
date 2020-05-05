package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService

class RemoveVideoFromCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionRetrievalService: CollectionRetrievalService
) {
    operator fun invoke(collectionId: String?, videoId: String?, user: User) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionRetrievalService.findWritable(CollectionId(value = collectionId), user = user)
            ?: throw CollectionNotFoundException(collectionId)

        collectionRepository.update(
            CollectionUpdateCommand.RemoveVideoFromCollection(
                collectionId = CollectionId(collectionId),
                videoId = VideoId(videoId),
                user = user
            )
        )
    }
}
