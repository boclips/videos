package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.analysis.GroupEventsByType
import com.boclips.videos.service.infrastructure.event.analysis.GroupRelatedEvents
import com.boclips.videos.service.infrastructure.event.analysis.Interaction
import com.boclips.videos.service.infrastructure.event.analysis.Interaction.Companion.fromPlaybackEvents
import com.boclips.videos.service.infrastructure.event.analysis.Interaction.Companion.fromSearchAndPlaybackEvents
import com.boclips.videos.service.infrastructure.event.analysis.Interaction.Companion.sortRecursively
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Component
data class LookbackHours(var search: Long = 24, var playback: Long = 24)

@Component
@ConfigurationProperties(prefix = "event.monitoring")
data class EventMonitoringConfig(val lookbackHours: LookbackHours)

data class EventsStatus(
        val healthy: Boolean,
        val latestSearch: ZonedDateTime?,
        val latestPlaybackInSearch: ZonedDateTime?,
        val latestPlaybackStandalone: ZonedDateTime?
)

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

        val mostRecentSearch = mostRecentEventByType(typeEquals("SEARCH"))
        val mostRecentPlaybackInSearch = mostRecentEventByType(typeEquals("PLAYBACK").andOperator(searchIdSpecified()))
        val mostRecentPlaybackStandalone = mostRecentEventByType(typeEquals("PLAYBACK").andOperator(searchIdNotSpecified()))

        val recentSearchExists = mostRecentSearch?.isAfter(utcNow.minusHours(eventMonitoringConfig.lookbackHours.search)) ?: false
        val recentPlaybackExists = mostRecentPlaybackInSearch?.isAfter(utcNow.minusHours(eventMonitoringConfig.lookbackHours.playback)) ?: false

        val healthy = recentSearchExists && recentPlaybackExists

        return EventsStatus(
                healthy = healthy,
                latestSearch = mostRecentSearch,
                latestPlaybackInSearch = mostRecentPlaybackInSearch,
                latestPlaybackStandalone = mostRecentPlaybackStandalone
        )
    }

    fun latestInteractions(): List<Interaction> {
        val events = eventLogRepository.findAll().map { it.toEvent() }

        val (allSearchEvents, allPlaybackEvents) = GroupEventsByType.groupByType(events)

        val relatedEvents = GroupRelatedEvents.create(allSearchEvents, allPlaybackEvents)

        val interactions = fromPlaybackEvents(relatedEvents.standalonePlaybacks) + fromSearchAndPlaybackEvents(relatedEvents.searches)

        return sortRecursively(interactions)
    }

    private fun mostRecentEventByType(criteria: Criteria): ZonedDateTime? {
        val filterByType = Aggregation.match(criteria)
        val findMax = Aggregation.project("type").andExpression("max(timestamp)").`as`("timestamp")

        val result = mongoTemplate.aggregate(Aggregation.newAggregation(Event::class.java, listOf(
                filterByType,
                findMax
        )), "event-log", MaxTimestampAggregationResult::class.java).mappedResults

        return if(result.isEmpty()) null else result[0].timestamp.atZone(ZoneOffset.UTC)
    }

    private fun typeEquals(type: String) = Criteria("type").isEqualTo(type)

    private fun searchIdSpecified() = Criteria("data.searchId").ne(null)

    private fun searchIdNotSpecified() = Criteria("data.searchId").`is`(null)


}

data class MaxTimestampAggregationResult(val timestamp: LocalDateTime)
