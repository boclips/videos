package com.boclips.videoanalyser.infrastructure.kaltura

import com.boclips.videoanalyser.domain.model.MediaFilter
import com.boclips.videoanalyser.domain.model.MediaFilterType
import com.boclips.videoanalyser.infrastructure.kaltura.client.KalturaMediaClient
import mu.KLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset
import java.util.stream.Collectors
import java.util.stream.IntStream

@Component
class PaginationOrchestrator(private val kalturaMediaClient: KalturaMediaClient,
                             private val maxEntries: Int,
                             private val pageSize: Int) {
    companion object : KLogging()

    fun fetchAll(searchFilters: List<MediaFilter> = emptyList()): List<MediaItem> {
        val startTimeBeforeTimeOfBoclips = LocalDateTime.of(2013, Month.JANUARY, 1, 0, 0, 0)
        val endTimeWithTimezoneBuffer = LocalDateTime.now().plusDays(1)

        logger.info("Start fetching Media Entries from $startTimeBeforeTimeOfBoclips to $endTimeWithTimezoneBuffer with filters $searchFilters")
        return fetchOrSplit(searchFilters, startTimeBeforeTimeOfBoclips.toEpochSecond(ZoneOffset.UTC), endTimeWithTimezoneBuffer.toEpochSecond(ZoneOffset.UTC))
    }

    private fun fetchOrSplit(filters: List<MediaFilter>, dateStart: Long, dateEnd: Long): List<MediaItem> {
        val timeFilters = createTimeFilters(dateEnd, dateStart)
        val numberOfEntriesForInterval = kalturaMediaClient.count(filters + timeFilters)
        logger.info("Found $numberOfEntriesForInterval entries to be fetched for current request ($dateStart until $dateEnd)")

        return if (numberOfEntriesForInterval == 0L) {
            logger.info("Aborting execution as no videos in interval found")
            return emptyList()
        } else if (numberOfEntriesForInterval > maxEntries) {
            logger.info("Splitting time interval in half as $numberOfEntriesForInterval is greater than $maxEntries")
            val mid = (dateEnd - dateStart) / 2
            val result = fetchOrSplit(filters, dateStart, dateStart + mid) + fetchOrSplit(filters, dateStart + mid, dateEnd)

            val uniqueResults = result.map { it.id }.toSet()
            if(uniqueResults.size.toLong() < numberOfEntriesForInterval) {
                throw Error("Expected $numberOfEntriesForInterval in time range ($dateStart until $dateEnd) but retrieved ${uniqueResults.size} (after the split)")
            }

            logger.info("Combined results from sub-intervals of ($dateStart until $dateEnd) have the expected size $numberOfEntriesForInterval")
            return result
        } else {
            logger.info("Fetching $numberOfEntriesForInterval entries for interval " +
                    "${Instant.ofEpochSecond(dateStart).atZone(ZoneOffset.UTC).toOffsetDateTime()} - " +
                    "${Instant.ofEpochSecond(dateEnd).atZone(ZoneOffset.UTC).toOffsetDateTime()}")
            val result = fetchPages(filters + timeFilters)
            val uniqueResults = result.map { it.id }.toSet()
            if(uniqueResults.size.toLong() < numberOfEntriesForInterval) {
                logger.error("Expected $numberOfEntriesForInterval in time range ($dateStart until $dateEnd) but retrieved ${uniqueResults.size}")
            }
            logger.info("Results from time range ($dateStart until $dateEnd) have the expected size $numberOfEntriesForInterval")
            return result
        }
    }

    private fun createTimeFilters(dateEnd: Long, dateStart: Long): List<MediaFilter> {
        return listOf(
                MediaFilter(MediaFilterType.CREATED_AT_LESS_THAN_OR_EQUAL, dateEnd.toString()),
                MediaFilter(MediaFilterType.CREATED_AT_GREATER_THAN_OR_EQUAL, dateStart.toString())
        )
    }

    private fun fetchPages(filters: List<MediaFilter>): List<MediaItem> {
        val count = kalturaMediaClient.count(filters = filters)
        val numberOfRequests = Math.ceil(count.toDouble() / pageSize.toDouble()).toInt()
        logger.info("Paging request ($numberOfRequests pages) to fetch $count entries")

        return IntStream.range(0, numberOfRequests).map{ it + 1 }.mapToObj { it }.parallel()
                .flatMap { i ->
                    logger.info("Fetching page $i with filters $filters")
                    val result = kalturaMediaClient.fetch(pageIndex = i, filters = filters)
                    logger.info("Fetched ${result.size} entries with filters $filters")
                    result.stream()
                }
                .collect(Collectors.toList())
    }
}