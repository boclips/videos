package com.boclips.videos.service.infrastructure.event.types

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class User(
        val boclipsEmployee: Boolean
) {
    companion object {
        fun fromEmail(email: String) = User(boclipsEmployee = email.endsWith("@boclips.com"))
        fun anonymous() = User(boclipsEmployee = false)
    }
}

open class Event<TData>(
        val type: String,
        val timestamp: ZonedDateTime,
        val user: User,
        val data: TData)

@Document(collection = "event-log")
class EventEntity(
        val type: String,
        val timestamp: LocalDateTime,
        val boclipsEmployee: Boolean = true,
        val data: Map<String, Any?>
) {

    fun toEvent(): Event<*> {
        val timestamp = timestamp.atZone(ZoneOffset.UTC)
        val user = User(boclipsEmployee = boclipsEmployee)

        return when (type) {
            EventType.SEARCH.name -> SearchEvent(
                    timestamp = timestamp,
                    correlationId = data["searchId"] as String,
                    user = user,
                    resultsReturned = data["resultsReturned"] as Int,
                    query = data["query"] as String
            )
            EventType.PLAYBACK.name -> PlaybackEvent(
                    playerId = data["playerId"] as String,
                    searchId = data["searchId"] as String?,
                    captureTime = timestamp,
                    user = user,
                    videoId = data["videoId"] as String,
                    segmentStartSeconds = data["segmentStartSeconds"] as Long,
                    segmentEndSeconds = data["segmentEndSeconds"] as Long,
                    videoDurationSeconds = data["videoDurationSeconds"] as Long
            )
            EventType.NO_SEARCH_RESULTS.name -> NoSearchResultsEvent(
                    name = data["name"] as String?,
                    email = data["email"] as String,
                    captureTime = timestamp,
                    user = user,
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
                    boclipsEmployee = event.user.boclipsEmployee,
                    timestamp = event.timestamp.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                    data = ObjectMapper().convertValue(event.data, object : TypeReference<Map<String, Any?>>() {})
            )
        }
    }
}
