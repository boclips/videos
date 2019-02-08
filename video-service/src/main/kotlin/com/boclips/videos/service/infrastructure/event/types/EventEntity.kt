package com.boclips.videos.service.infrastructure.event.types

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneOffset

@Document(collection = "event-log")
class EventEntity(
    val type: String,
    val timestamp: LocalDateTime,
    val user: UserEntity = UserEntity(true, "ANONYMOUS-TRACKED-BEFORE-REGISTERING-USERS"),
    val data: Map<String, Any?>
) {

    fun toEvent(): Event<*> {
        val timestamp = timestamp.atZone(ZoneOffset.UTC)
        val convertedUser = User(boclipsEmployee = user.boclipsEmployee, id = user.id)

        return when (EventType.valueOf(type)) {
            EventType.SEARCH -> SearchEvent(
                timestamp = timestamp,
                correlationId = data["searchId"] as String,
                user = convertedUser,
                resultsReturned = data["resultsReturned"] as Int,
                query = data["query"] as String
            )
            EventType.PLAYBACK -> PlaybackEvent(
                playerId = data["playerId"] as String,
                searchId = data["searchId"] as String?,
                captureTime = timestamp,
                user = convertedUser,
                videoId = data["videoId"] as String,
                segmentStartSeconds = data["segmentStartSeconds"] as Long,
                segmentEndSeconds = data["segmentEndSeconds"] as Long,
                videoDurationSeconds = data["videoDurationSeconds"] as Long
            )
            EventType.NO_SEARCH_RESULTS -> NoSearchResultsEvent(
                name = data["name"] as String?,
                email = data["email"] as String,
                captureTime = timestamp,
                user = convertedUser,
                query = data["query"] as String,
                description = data["description"] as String?
            )
            EventType.ADD_VIDEO_TO_COLLECTION -> AddToCollectionEvent(
                timestamp = timestamp,
                user = convertedUser,
                videoId = data["videoId"] as String,
                collectionId = data["collectionId"] as String
            )
            EventType.REMOVE_VIDEO_FROM_COLLECTION -> RemoveFromCollectionEvent(
                timestamp = timestamp,
                user = convertedUser,
                videoId = data["videoId"] as String,
                collectionId = data["collectionId"] as String
            )
        }
    }

    companion object {
        fun fromEvent(event: Event<*>): EventEntity {
            return EventEntity(
                type = event.type,
                user = UserEntity(
                    boclipsEmployee = event.user.boclipsEmployee,
                    id = event.user.id
                ),
                timestamp = event.timestamp.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                data = ObjectMapper().convertValue(event.data, object : TypeReference<Map<String, Any?>>() {})
            )
        }
    }
}