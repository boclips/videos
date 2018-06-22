package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.domain.model.KalturaVideo
import com.boclips.cleanser.domain.model.MediaFilter
import com.boclips.cleanser.domain.model.MediaFilterType
import com.boclips.cleanser.domain.service.KalturaMediaService
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class PagedKalturaMediaService(
        private val kalturaMediaClient: KalturaMediaClient,
        private val paginationOrchestrator: PaginationOrchestrator) : KalturaMediaService {
    companion object : KLogging()

    override fun countAllMediaEntries(): Long {
        return kalturaMediaClient.count(listOf(MediaFilter(MediaFilterType.STATUS_IN, "2"))) +
                kalturaMediaClient.count(listOf(MediaFilter(MediaFilterType.STATUS_NOT_EQUAL, "2")))
    }

    override fun getReadyMediaEntries(): Set<KalturaVideo> {
        val searchFilters: List<MediaFilter> = listOf(MediaFilter(MediaFilterType.STATUS_IN, "2"))
        val readyKalturaVideos = fetch(searchFilters)
        logger.info("Returning ${readyKalturaVideos.size} READY Kaltura Videos")
        return readyKalturaVideos
    }

    override fun getPendingMediaEntries(): Set<KalturaVideo> {
        val searchFilters: List<MediaFilter> = listOf(MediaFilter(MediaFilterType.STATUS_IN, "4"))
        val pendingKalturaVideos = fetch(searchFilters)
        logger.info("Returning ${pendingKalturaVideos.size} PENDING Kaltura Videos")
        return pendingKalturaVideos
    }

    override fun getFaultyMediaEntries(): Set<KalturaVideo> {
        val searchFilters: List<MediaFilter> = listOf(MediaFilter(MediaFilterType.STATUS_NOT_EQUAL, "2"))
        val faultyKalturaVideos = fetch(searchFilters)
        logger.info("Returning ${faultyKalturaVideos.size} faulty Kaltura Videos")
        return faultyKalturaVideos
    }

    private fun fetch(searchFilters: List<MediaFilter> = emptyList()): Set<KalturaVideo> {
        return paginationOrchestrator
                .fetchAll(searchFilters)
                .filter { item ->
                    if (item.referenceId == null) {
                        logger.warn("Retrieved MediaItem with null referenceId: $item")
                        false
                    } else {
                        true
                    }
                }
                .map { KalturaVideo(referenceId = it.referenceId!!, id = it.id) }
                .toSet()
    }
}