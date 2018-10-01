package com.boclips.videos.service.infrastructure.event

import java.time.ZonedDateTime

data class SearchEventData(val searchId: String, val query: String, val resultsReturned: Int)

class SearchEvent(timestamp: ZonedDateTime, searchId: String, query: String, resultsReturned: Int) : Event<SearchEventData>("SEARCH", timestamp, SearchEventData(searchId, query, resultsReturned))
