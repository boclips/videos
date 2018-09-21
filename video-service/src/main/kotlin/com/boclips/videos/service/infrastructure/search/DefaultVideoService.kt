package com.boclips.videos.service.infrastructure.search

import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.SearchResults
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.search.VideoInformationAggregator.convert
import java.time.ZonedDateTime
import java.util.*

data class SearchEventData(val searchId: String, val query: String, val resultsReturned: Int)

class SearchEvent(timestamp: ZonedDateTime, searchId: String, query: String, resultsReturned: Int) : Event<SearchEventData>("SEARCH", timestamp, SearchEventData(searchId, query, resultsReturned))

class DefaultVideoService(
        private val searchService: SearchService,
        private val eventService: EventService,
        private val kalturaClient: KalturaClient
) : VideoService {
    companion object {
        fun extractKalturaReferenceIds(videos: List<ElasticSearchVideo>) =
                videos.map { it.referenceId }.toTypedArray()
    }

    override fun findById(id: String): Video? {
        return searchService.findById(id)
                ?.let {
                    val mediaEntry = kalturaClient.mediaEntryByReferenceId(it.referenceId).orElse(null)
                            ?: return@let null
                    convert(it, mediaEntry)
                }
    }

    override fun search(query: String): SearchResults {
        val searchResults = searchService.search(query)

        val referenceIds = extractKalturaReferenceIds(searchResults.videos)
        val mediaEntries = kalturaClient.mediaEntriesByReferenceIds(*referenceIds)

        val id = UUID.randomUUID().toString()

        eventService.saveEvent(SearchEvent(ZonedDateTime.now(), id, query, searchResults.videos.size))

        val videos = convert(searchResults.videos, mediaEntries)

        return SearchResults(searchId = id, query = query, videos = videos)
    }
}
