package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService

data class SearchEventData(val query: String, val resultsReturned: Int)

class SearchEvent(query: String, resultsReturned: Int) : Event<SearchEventData>("SEARCH", SearchEventData(query, resultsReturned))

class EventLoggingSearchService(
        private val searchService: SearchService,
        private val eventService: EventService
) : SearchService {

    override fun search(query: String): List<Video> {
        return searchService.search(query)
                .apply { eventService.saveEvent(SearchEvent(query, size)) }
    }
}