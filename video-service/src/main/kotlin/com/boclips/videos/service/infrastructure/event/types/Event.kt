package com.boclips.videos.service.infrastructure.event.types

import java.time.ZonedDateTime

open class Event<TData>(
    val type: String,
    val timestamp: ZonedDateTime,
    val user: User,
    val data: TData
)