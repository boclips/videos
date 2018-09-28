package com.boclips.videos.service.infrastructure.search

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.RequestId
import com.boclips.videos.service.infrastructure.search.VideoInformationAggregator.convert
import mu.KLogging
import java.time.ZonedDateTime
import java.util.*

data class SearchEventData(val searchId: String, val query: String, val resultsReturned: Int)

class SearchEvent(timestamp: ZonedDateTime, searchId: String, query: String, resultsReturned: Int) : Event<SearchEventData>("SEARCH", timestamp, SearchEventData(searchId, query, resultsReturned))

class DefaultVideoService(
        private val searchService: SearchService,
        private val eventService: EventService,
        private val kalturaClient: KalturaClient,
        private val requestId: RequestId
) : VideoService {
    companion object : KLogging() {
        fun extractKalturaReferenceIds(videos: List<ElasticSearchVideo>) =
                videos.map { it.referenceId }
    }

    override fun findById(id: String): Video? {
        val elasticSearchVideo = searchService.findById(id) ?: return null
        val kalturaVideo = kalturaClient.getMediaEntriesByReferenceId(elasticSearchVideo.referenceId).firstOrNull()
                ?: return null

        return convert(elasticSearchVideo, kalturaVideo)
    }

    override fun search(query: String): List<Video> {
        val searchResults = searchService.search(query)

        val referenceIds = extractKalturaReferenceIds(searchResults.videos)
        logger.info("Retrieving media entries for reference ids: ${referenceIds.joinToString(",")}")
        val mediaEntries: Map<String, MediaEntry> = getKalturaVideoByReferenceId(referenceIds)

        requestId.id = UUID.randomUUID().toString()

        eventService.saveEvent(SearchEvent(ZonedDateTime.now(), requestId.id!!, query, searchResults.videos.size))

        return convert(searchResults.videos, mediaEntries)
    }

    fun getKalturaVideoByReferenceId(referenceIds: List<String>): Map<String, MediaEntry> {
        val mediaEntries: Map<String, List<MediaEntry>> = kalturaClient.getMediaEntriesByReferenceIds(referenceIds)
        return mediaEntries.mapValues { it.value.first() }
    }
}
