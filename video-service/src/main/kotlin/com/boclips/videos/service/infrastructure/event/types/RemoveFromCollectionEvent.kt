package com.boclips.videos.service.infrastructure.event.types

import java.time.ZonedDateTime

data class RemoveFromCollectionEventData(
        val collectionId: String,
        val videoId: String
)

class RemoveFromCollectionEvent(
        timestamp: ZonedDateTime,
        user: User,
        collectionId: String,
        videoId: String
) : Event<RemoveFromCollectionEventData>(EventType.REMOVE_VIDEO_FROM_COLLECTION.name, timestamp, user, RemoveFromCollectionEventData(collectionId, videoId))
