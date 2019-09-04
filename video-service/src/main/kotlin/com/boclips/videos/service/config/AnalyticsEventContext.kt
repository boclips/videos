package com.boclips.videos.service.config

import com.boclips.eventbus.EventBus

import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.application.analytics.SavePlayerInteractedWithEvent
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.infrastructure.analytics.PubSubEventsService
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AnalyticsEventContext(private val eventBus: EventBus) {

    @Bean
    fun analyticsEventService(mongoClient: MongoClient): EventService = PubSubEventsService(eventBus)

    @Bean
    fun savePlaybackEvent(eventService: EventService): SavePlaybackEvent {
        return SavePlaybackEvent(eventService = eventService)
    }

    @Bean
    fun savePlayerInteractedWithEvent(eventService: EventService): SavePlayerInteractedWithEvent {
        return SavePlayerInteractedWithEvent(eventService = eventService)
    }
}
