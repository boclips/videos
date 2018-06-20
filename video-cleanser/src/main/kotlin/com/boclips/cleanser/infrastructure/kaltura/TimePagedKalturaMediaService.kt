package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.domain.model.MediaFilter
import com.boclips.cleanser.domain.model.MediaFilterType
import com.boclips.cleanser.domain.model.MediaItem
import com.boclips.cleanser.domain.service.KalturaMediaService
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import mu.KLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset

@Component
class TimePagedKalturaMediaService(val kalturaMediaClient: KalturaMediaClient) : KalturaMediaService {
    override fun countAllMediaEntries(): Long {
        return kalturaMediaClient.count(listOf(MediaFilter(MediaFilterType.STATUS_IN, "2"))) +
                kalturaMediaClient.count(listOf(MediaFilter(MediaFilterType.STATUS_NOT_EQUAL, "2")))
    }

    override fun getReadyMediaEntries(): Set<MediaItem> {
        val searchFilters: List<MediaFilter> = listOf(MediaFilter(MediaFilterType.STATUS_IN, "2"))
        return fetch(searchFilters)
    }

    override fun getFaultyMediaEntries(): Set<MediaItem> {
        val searchFilters: List<MediaFilter> = listOf(MediaFilter(MediaFilterType.STATUS_NOT_EQUAL, "2"))
        return fetch(searchFilters)
    }

    private fun fetch(searchFilters: List<MediaFilter> = emptyList()): Set<MediaItem> {
        return TimePagingFetcher(kalturaMediaClient, 10000, 500)
                .fetchAll(searchFilters)
                .toSet()
    }
}

class TimePagingFetcher(private val kalturaMediaClient: KalturaMediaClient, private val maxEntries: Int, private val pageSize: Int) {
    companion object : KLogging()

    fun fetchAll(searchFilters: List<MediaFilter> = emptyList()): List<MediaItem> {
        val dateStart = LocalDateTime.of(2018, Month.JANUARY, 1, 0, 0, 0).toEpochSecond(ZoneOffset.UTC)
        val dateEnd = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

        return fetchOrSplit(searchFilters, dateStart, dateEnd)
    }

    private fun fetchOrSplit(filters: List<MediaFilter>, dateStart: Long, dateEnd: Long): List<MediaItem> {
        val timeFilters = listOf(
                MediaFilter(MediaFilterType.CREATED_AT_LESS_THAN_OR_EQUAL, dateEnd.toString()),
                MediaFilter(MediaFilterType.CREATED_AT_GREATER_THAN_OR_EQUAL, dateStart.toString())
        )
        val numberOfEntriesForInterval = kalturaMediaClient.count(filters + timeFilters)
        logger.info("Found $numberOfEntriesForInterval entries to be fetched for current request")

        return if (numberOfEntriesForInterval > maxEntries) {
            logger.info("Splitting time interval in half as $numberOfEntriesForInterval is greater than $maxEntries")
            val mid = (dateEnd - dateStart) / 2
            fetchOrSplit(filters, dateStart, dateStart + mid) + fetchOrSplit(filters, dateStart + mid, dateEnd)
        } else {
            logger.info("Fetching $numberOfEntriesForInterval entries")
            fetchPages(filters + timeFilters)
        }
    }

    private fun fetchPages(filters: List<MediaFilter>): List<MediaItem> {
        val count = kalturaMediaClient.count(filters = filters)
        val numberOfRequests = Math.ceil(count.toDouble() / pageSize.toDouble()).toInt()

        return IntRange(0, numberOfRequests)
                .flatMap { i ->
                    logger.info("Fetching page $i with filters $filters")
                    kalturaMediaClient.fetch(pageIndex = i, filters = filters)
                }
    }
}