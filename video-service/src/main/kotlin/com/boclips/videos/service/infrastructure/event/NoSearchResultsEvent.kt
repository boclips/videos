package com.boclips.videos.service.infrastructure.event

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
        query: String?,
        description: String?,
        captureTime: ZonedDateTime
) : Event<NoSearchResultsEventData>("NO_RESULTS_FOUND", captureTime, NoSearchResultsEventData(
        name = name,
        email = email,
        query = query,
        description = description
))