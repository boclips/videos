package com.boclips.videos.service.infrastructure.event

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Component
data class LookbackHours(var search: Long = 24, var playback: Long = 24)

@Component
@ConfigurationProperties(prefix = "event.monitoring")
data class EventMonitoringConfig(val lookbackHours: LookbackHours)

class EventService(
        private val eventLogRepository: EventLogRepository,
        private val eventMonitoringConfig: EventMonitoringConfig
) {
    fun <T> saveEvent(event: Event<T>) {
        eventLogRepository.insert(event)
    }

    fun status(): Boolean {
        val utcNow = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
        return eventLogRepository.countByTypeAfter("PLAYBACK", utcNow.minusHours(eventMonitoringConfig.lookbackHours.playback)) > 0 &&
                eventLogRepository.countByTypeAfter("SEARCH", utcNow.minusHours(eventMonitoringConfig.lookbackHours.search)) > 0
    }
}
