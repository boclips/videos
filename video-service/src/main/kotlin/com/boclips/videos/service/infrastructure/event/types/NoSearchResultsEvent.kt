package com.boclips.videos.service.infrastructure.event.types

import java.time.ZonedDateTime

data class NoSearchResultsEventData(
        val name: String?,
        val email: String?,
        val query: String?,
        val description: String?
)

class NoSearchResultsEvent(
        name: String?,
        email: String?,
        user: User,
        query: String?,
        description: String?,
        captureTime: ZonedDateTime
) : Event<NoSearchResultsEventData>(EventType.NO_SEARCH_RESULTS.name, captureTime, user, NoSearchResultsEventData(
        name = name,
        email = email,
        query = query,
        description = description
))