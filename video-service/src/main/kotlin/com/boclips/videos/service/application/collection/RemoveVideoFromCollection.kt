package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.service.events.EventService

class RemoveVideoFromCollection(
    private val collectionRepository: CollectionRepository,
    private val eventService: EventService
) {
    operator fun invoke(collectionId: String?, videoId: String?) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionRepository.getOwnedCollectionOrThrow(collectionId)

        collectionRepository.update(
            CollectionId(collectionId),
            CollectionUpdateCommand.RemoveVideoFromCollection(VideoId(videoId))
        )

        eventService.saveUpdateCollectionEvent(
            CollectionId(collectionId),
            listOf(CollectionUpdateCommand.RemoveVideoFromCollection(VideoId(videoId)))
        )
    }
}