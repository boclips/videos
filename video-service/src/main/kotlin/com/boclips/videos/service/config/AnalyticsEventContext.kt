package com.boclips.videos.service.config

import com.boclips.events.config.Topics
import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.infrastructure.analytics.PubSubEventsService
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AnalyticsEventContext(private val topics: Topics) {

    @Bean
    fun analyticsEventService(mongoClient: MongoClient): EventService = PubSubEventsService(topics)

    @Bean
    fun savePlaybackEvent(eventService: EventService): SavePlaybackEvent {
        return SavePlaybackEvent(eventService = eventService)
    }
}