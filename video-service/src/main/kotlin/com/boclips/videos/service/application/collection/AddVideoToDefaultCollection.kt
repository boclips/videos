package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.collection.AddVideoToCollection
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.infrastructure.event.EventService

class AddVideoToDefaultCollection(
    private val collectionService: CollectionService,
    private val eventService: EventService
) {
    operator fun invoke(videoId: String?) {
        videoId ?: throw Exception("Video id cannot be null")

        val user = UserExtractor.getCurrentUser()
        val userId = UserId(value = user.id)

        if (collectionService.getByOwner(userId).isEmpty()) {
            collectionService.create(owner = userId, title = Collection.DEFAULT_TITLE)
        }

        val collection = collectionService.getByOwner(userId).first()

        collectionService.update(
            collection.id,
            AddVideoToCollection(AssetId(videoId))
        )

        eventService.saveAddToCollectionEvent(collectionId = collection.id, videoId = AssetId(videoId))
    }
}