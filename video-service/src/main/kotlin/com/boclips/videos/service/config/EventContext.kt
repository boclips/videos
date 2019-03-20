package com.boclips.videos.service.config

import com.boclips.videos.service.application.event.SavePlaybackEvent
import com.boclips.videos.service.infrastructure.event.AnalyticsEventService
import com.boclips.videos.service.infrastructure.event.MongoAnalyticsEventService
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventContext {

    @Bean
    fun eventService(mongoClient: MongoClient): AnalyticsEventService = MongoAnalyticsEventService(mongoClient)

    @Bean
    fun savePlaybackEvent(analyticsEventService: AnalyticsEventService): SavePlaybackEvent {
        return SavePlaybackEvent(analyticsEventService = analyticsEventService)
    }
}