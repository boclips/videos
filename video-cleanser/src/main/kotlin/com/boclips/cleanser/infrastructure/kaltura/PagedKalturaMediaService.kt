package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.domain.model.MediaFilter
import com.boclips.cleanser.domain.model.MediaFilterType
import com.boclips.cleanser.domain.service.KalturaMediaService
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import org.springframework.stereotype.Component

@Component
class PagedKalturaMediaService(
        private val kalturaMediaClient: KalturaMediaClient,
        private val paginationOrchestrator: PaginationOrchestrator) : KalturaMediaService {

    override fun countAllMediaEntries(): Long {
        return kalturaMediaClient.count(listOf(MediaFilter(MediaFilterType.STATUS_IN, "2"))) +
                kalturaMediaClient.count(listOf(MediaFilter(MediaFilterType.STATUS_NOT_EQUAL, "2")))
    }

    override fun getReadyMediaEntries(): Set<String> {
        val searchFilters: List<MediaFilter> = listOf(MediaFilter(MediaFilterType.STATUS_IN, "2"))
        return fetch(searchFilters)
    }

    override fun getFaultyMediaEntries(): Set<String> {
        val searchFilters: List<MediaFilter> = listOf(MediaFilter(MediaFilterType.STATUS_NOT_EQUAL, "2"))
        return fetch(searchFilters)
    }

    private fun fetch(searchFilters: List<MediaFilter> = emptyList()): Set<String> {
        return paginationOrchestrator
                .fetchAll(searchFilters)
                .map { it.referenceId }
                .toSet()
    }
}