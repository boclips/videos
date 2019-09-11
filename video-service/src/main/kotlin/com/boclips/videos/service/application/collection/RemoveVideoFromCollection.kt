package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.events.EventService

class RemoveVideoFromCollection(
    private val collectionRepository: CollectionRepository,
    private val eventService: EventService,
    private val collectionService: CollectionService
) {
    operator fun invoke(collectionId: String?, videoId: String?) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionService.getOwnedCollectionOrThrow(collectionId)

        collectionRepository.update(
            CollectionUpdateCommand.RemoveVideoFromCollection(
                collectionId = CollectionId(collectionId),
                videoId = VideoId(videoId)
            )
        )

        eventService.saveUpdateCollectionEvent(
            listOf(
                CollectionUpdateCommand.RemoveVideoFromCollection(
                    collectionId = CollectionId(collectionId),
                    videoId = VideoId(videoId)
                )
            )
        )
    }
}
