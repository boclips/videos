package com.boclips.videos.service.infrastructure.search

import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.search.VideoInformationAggregator.convert

data class SearchEventData(val query: String, val resultsReturned: Int)

class SearchEvent(query: String, resultsReturned: Int) : Event<SearchEventData>("SEARCH", SearchEventData(query, resultsReturned))

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

    override fun find(query: String): List<Video> {
        val searchResults = searchService.search(query)

        val referenceIds = extractKalturaReferenceIds(searchResults.videos)
        val mediaEntries = kalturaClient.mediaEntriesByReferenceIds(*referenceIds)

        eventService.saveEvent(SearchEvent(query, searchResults.videos.size))

        return convert(searchResults.videos, mediaEntries)
    }
}