package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
import com.boclips.videos.service.infrastructure.event.EventService

class RemoveVideoFromCollection(
    private val collectionService: CollectionService,
    private val eventService: EventService
) {
    operator fun invoke(collectionId: String?, videoId: String?) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionService.update(
            id = CollectionId(collectionId),
            updateCommand = RemoveVideoFromCollectionCommand(AssetId(videoId))
        )

        eventService.saveRemoveFromCollectionEvent(
            collectionId = CollectionId(collectionId),
            videoId = AssetId(videoId)
        )
    }
}