package com.boclips.videos.service.infrastructure.event.types

import java.time.ZonedDateTime

data class SearchEventData(
    val searchId: String,
    val query: String,
    val resultsReturned: Int
)

class SearchEvent(
    timestamp: ZonedDateTime,
    correlationId: String,
    user: User,
    query: String,
    resultsReturned: Int
) : Event<SearchEventData>(
    EventType.SEARCH.name,
    timestamp,
    user,
    SearchEventData(correlationId, query, resultsReturned)
)
