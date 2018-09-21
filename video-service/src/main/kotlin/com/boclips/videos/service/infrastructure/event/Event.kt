package com.boclips.videos.service.infrastructure.event

import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Document(collection = "event-log")
open class Event<TData>(val type: String, timestamp: ZonedDateTime, val data: TData) {
    val timestamp = timestamp.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()!!
}
