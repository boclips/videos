package com.boclips.videos.service.config

import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.application.event.GetEvent
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventMonitoringConfig
import com.boclips.videos.service.infrastructure.event.EventService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class EventContext {
    @Bean
    fun eventService(
        eventLogRepository: EventLogRepository,
        eventMonitoringConfig: EventMonitoringConfig,
        mongoTemplate: MongoTemplate
    ) =
        EventService(
            eventLogRepository = eventLogRepository,
            eventMonitoringConfig = eventMonitoringConfig,
            mongoTemplate = mongoTemplate
        )

    @Bean
    fun createEvent(eventService: EventService, emailClient: EmailClient): CreateEvent {
        return CreateEvent(
            eventService = eventService,
            emailClient = emailClient
        )
    }

    @Bean
    fun getEvent(eventService: EventService): GetEvent {
        return GetEvent(eventService = eventService)
    }

    @Bean
    fun checkEventsStatus(eventService: EventService): CheckEventsStatus {
        return CheckEventsStatus(
            eventService = eventService
        )
    }
}