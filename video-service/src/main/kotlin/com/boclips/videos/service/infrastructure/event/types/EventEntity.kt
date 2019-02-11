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