package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.Event
import java.time.ZonedDateTime

data class SearchEventData(val searchId: String, val query: String, val resultsReturned: Int)

class SearchEvent(timestamp: ZonedDateTime, searchId: String, query: String, resultsReturned: Int) : Event<SearchEventData>("SEARCH", timestamp, SearchEventData(searchId, query, resultsReturned))

class DefaultVideoService : VideoService
