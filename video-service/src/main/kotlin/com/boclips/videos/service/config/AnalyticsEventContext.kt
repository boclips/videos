package com.boclips.videos.service.config

import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import com.boclips.videos.service.infrastructure.analytics.MongoAnalyticsEventService
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AnalyticsEventContext {

    @Bean
    fun analyticsEventService(mongoClient: MongoClient): AnalyticsEventService = MongoAnalyticsEventService(mongoClient)

    @Bean
    fun savePlaybackEvent(analyticsEventService: AnalyticsEventService): SavePlaybackEvent {
        return SavePlaybackEvent(analyticsEventService = analyticsEventService)
    }
}