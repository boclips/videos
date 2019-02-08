package com.boclips.videos.service.infrastructure.event.types

import java.time.ZonedDateTime

data class AddToCollectionEventData(
    val collectionId: String,
    val videoId: String
)

class AddToCollectionEvent(
    timestamp: ZonedDateTime,
    user: User,
    collectionId: String,
    videoId: String
) : Event<AddToCollectionEventData>(
    EventType.ADD_VIDEO_TO_COLLECTION.name,
    timestamp,
    user,
    AddToCollectionEventData(collectionId, videoId)
)
