package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.AddVideoToCollection
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.AddToCollectionEvent
import com.boclips.videos.service.infrastructure.event.types.User
import java.time.ZonedDateTime

class AddVideoToDefaultCollection(
        private val collectionService: CollectionService,
        private val eventService: EventService
) {
    operator fun invoke(videoId: String?) {
        videoId ?: throw Exception("Video id cannot be null")

        val user = UserExtractor.getCurrentUser()
        val userId = user.id

        if (collectionService.getByOwner(UserId(value = userId)).isEmpty()) {
            collectionService.create(UserId(value = userId))
        }

        val collection = collectionService.getByOwner(UserId(value = userId)).first()

        collectionService.update(collection.id, AddVideoToCollection(AssetId(videoId)))

        eventService.saveEvent(AddToCollectionEvent(
                timestamp = ZonedDateTime.now(),
                user = User.fromSecurityUser(user),
                collectionId = collection.id.value,
                videoId = videoId
        ))
    }
}