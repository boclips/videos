package com.boclips.videos.service.config

import com.boclips.videos.service.application.event.SavePlaybackEvent
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.MongoEventService
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventContext {

    @Bean
    fun eventService(mongoClient: MongoClient): EventService = MongoEventService(mongoClient)

    @Bean
    fun savePlaybackEvent(eventService: EventService): SavePlaybackEvent {
        return SavePlaybackEvent(eventService = eventService)
    }
}