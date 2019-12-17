package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

class RemoveVideoFromCollection(
    private val collectionRepository: CollectionRepository,
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String?, videoId: String?, user: User) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        val collection = collectionService.findWritable(CollectionId(value = collectionId), user = user)
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
