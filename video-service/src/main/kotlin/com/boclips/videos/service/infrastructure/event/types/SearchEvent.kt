package com.boclips.videos.service.infrastructure.event.types

import java.time.ZonedDateTime

data class SearchEventData(
    val query: String,
    val page: Int,
    val resultsReturned: Int
)

class SearchEvent(
    timestamp: ZonedDateTime,
    user: User,
    page: Int,
    query: String,
    resultsReturned: Int
) : Event<SearchEventData>(
    EventType.SEARCH.name,
    timestamp,
    user,
    SearchEventData(query, page, resultsReturned)
)
