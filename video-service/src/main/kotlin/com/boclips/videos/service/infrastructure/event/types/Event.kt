package com.boclips.videos.service.infrastructure.event.types

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class User(
        val boclipsEmployee: Boolean,
        val id: String
) {
    companion object {
        fun fromEmail(email: String, id: String) = User(boclipsEmployee = email.endsWith("@boclips.com"), id = id)
        fun anonymous() = User(boclipsEmployee = false, id = "ANONYMOUS")
    }
}

open class Event<TData>(
        val type: String,
        val timestamp: ZonedDateTime,
        val user: User,
        val data: TData)


data class UserValueType(
        val boclipsEmployee: Boolean,
        val id: String
)

@Document(collection = "event-log")
class EventEntity(
        val type: String,
        val timestamp: LocalDateTime,
        val user: UserValueType = UserValueType(true, "ANONYMOUS-TRACKED-BEFORE-REGISTERING-USERS"),
        val data: Map<String, Any?>
) {

    fun toEvent(): Event<*> {
        val timestamp = timestamp.atZone(ZoneOffset.UTC)
        val convertedUser = User(boclipsEmployee = user.boclipsEmployee, id = user.id)

        return when (type) {
            EventType.SEARCH.name -> SearchEvent(
                    timestamp = timestamp,
                    correlationId = data["searchId"] as String,
                    user = convertedUser,
                    resultsReturned = data["resultsReturned"] as Int,
                    query = data["query"] as String
            )
            EventType.PLAYBACK.name -> PlaybackEvent(
                    playerId = data["playerId"] as String,
                    searchId = data["searchId"] as String?,
                    captureTime = timestamp,
                    user = convertedUser,
                    videoId = data["videoId"] as String,
                    segmentStartSeconds = data["segmentStartSeconds"] as Long,
                    segmentEndSeconds = data["segmentEndSeconds"] as Long,
                    videoDurationSeconds = data["videoDurationSeconds"] as Long
            )
            EventType.NO_SEARCH_RESULTS.name -> NoSearchResultsEvent(
                    name = data["name"] as String?,
                    email = data["email"] as String,
                    captureTime = timestamp,
                    user = convertedUser,
                    query = data["query"] as String,
                    description = data["description"] as String?
            )
            else -> throw RuntimeException(type)
        }
    }

    companion object {
        fun fromEvent(event: Event<*>): EventEntity {
            return EventEntity(
                    type = event.type,
                    user = UserValueType(
                            boclipsEmployee = event.user.boclipsEmployee,
                            id = event.user.id
                    ),
                    timestamp = event.timestamp.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                    data = ObjectMapper().convertValue(event.data, object : TypeReference<Map<String, Any?>>() {})
            )
        }
    }
}
