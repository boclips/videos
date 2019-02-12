package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.domain.service.RemoveVideoFromCollection
import com.boclips.videos.service.infrastructure.event.EventService

class RemoveVideoFromDefaultCollection(
        private val collectionService: CollectionService,
        private val eventService: EventService
) {
    operator fun invoke(videoId: String?) {
        videoId ?: throw Exception("Video id cannot be null")

        val user = UserExtractor.getCurrentUser()
        val collection = collectionService.getByOwner(UserId(value = user.id)).first()

        collectionService.update(collection.id, RemoveVideoFromCollection(AssetId(videoId)))

        eventService.saveRemoveFromCollectionEvent(
                collectionId = collection.id,
                videoId = AssetId(videoId)
        )
    }
}