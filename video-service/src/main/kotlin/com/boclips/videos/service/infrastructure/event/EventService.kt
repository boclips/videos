package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.analysis.GroupEventsByType
import com.boclips.videos.service.infrastructure.event.analysis.GroupRelatedEvents
import com.boclips.videos.service.infrastructure.event.analysis.Interaction
import com.boclips.videos.service.infrastructure.event.analysis.Interaction.Companion.fromPlaybackEvents
import com.boclips.videos.service.infrastructure.event.analysis.Interaction.Companion.fromSearchAndPlaybackEvents
import com.boclips.videos.service.infrastructure.event.analysis.Interaction.Companion.sortRecursively
import com.boclips.videos.service.infrastructure.event.types.Event
import com.boclips.videos.service.infrastructure.event.types.EventEntity
import com.boclips.videos.service.infrastructure.event.types.EventType
import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EventService(
    private val eventLogRepository: EventLogRepository,
    private val eventMonitoringConfig: EventMonitoringConfig,
    private val mongoTemplate: MongoTemplate
) {
    fun <T> saveEvent(event: Event<T>) {
        eventLogRepository.insert(EventEntity.fromEvent(event))
    }

    fun status(): EventsStatus {
        val utcNow = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC)

        val mostRecentSearch = mostRecentEventByType(typeEquals(EventType.SEARCH.name))
        val mostRecentPlaybackInSearch =
            mostRecentEventByType(typeEquals(EventType.PLAYBACK.name).andOperator(searchIdSpecified()))
        val mostRecentPlaybackStandalone =
            mostRecentEventByType(typeEquals(EventType.PLAYBACK.name).andOperator(searchIdNotSpecified()))

        val recentSearchExists =
            mostRecentSearch?.isAfter(utcNow.minusHours(eventMonitoringConfig.lookbackHours.search))
                ?: false
        val recentPlaybackExists =
            mostRecentPlaybackInSearch?.isAfter(utcNow.minusHours(eventMonitoringConfig.lookbackHours.playback))
                ?: false

        val healthy = recentSearchExists && recentPlaybackExists

        return EventsStatus(
            healthy = healthy,
            latestSearch = mostRecentSearch,
            latestPlaybackInSearch = mostRecentPlaybackInSearch,
            latestPlaybackStandalone = mostRecentPlaybackStandalone
        )
    }

    fun latestInteractions(): List<Interaction> {
        val events = getAllEvents()

        val (allSearchEvents, allPlaybackEvents) = GroupEventsByType.groupByType(events)

        val relatedEvents = GroupRelatedEvents.create(allSearchEvents, allPlaybackEvents)

        val interactions =
            fromPlaybackEvents(relatedEvents.standalonePlaybacks) + fromSearchAndPlaybackEvents(relatedEvents.searches)

        return sortRecursively(interactions)
    }

    fun getNoSearchResultsEvents(): List<NoSearchResultsEvent> {
        return getAllEvents()
            .filter { event -> event.type.equals(EventType.NO_SEARCH_RESULTS.name) } as List<NoSearchResultsEvent>
    }

    private fun getAllEvents() = eventLogRepository.findAll()
        .map { it.toEvent() }

    private fun mostRecentEventByType(criteria: Criteria): ZonedDateTime? {
        val filterByType = Aggregation.match(criteria)
        val findMax = Aggregation.project("type").andExpression("max(timestamp)").`as`("timestamp")

        val result = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Event::class.java, listOf(
                    filterByType,
                    findMax
                )
            ), "event-log", MaxTimestampAggregationResult::class.java
        ).mappedResults

        return if (result.isEmpty()) null else result[0].timestamp.atZone(ZoneOffset.UTC)
    }

    private fun typeEquals(type: String) = Criteria("type").isEqualTo(type)

    private fun searchIdSpecified() = Criteria("data.searchId").ne(null)

    private fun searchIdNotSpecified() = Criteria("data.searchId").`is`(null)
}
