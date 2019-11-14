package com.boclips.videos.service.config

import com.boclips.eventbus.EventBus

import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.application.analytics.SavePlayerInteractedWithEvent
import com.boclips.videos.service.application.analytics.SaveVideoInteractedWithEvent
import com.boclips.videos.service.domain.service.events.EventService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AnalyticsEventContext(private val eventService: EventService) {

    @Bean
    fun savePlaybackEvent(): SavePlaybackEvent {
        return SavePlaybackEvent(eventService)
    }

    @Bean
    fun savePlayerInteractedWithEvent(): SavePlayerInteractedWithEvent {
        return SavePlayerInteractedWithEvent(eventService)
    }

    @Bean
    fun saveVideoInteractedWithEvent(): SaveVideoInteractedWithEvent {
        return SaveVideoInteractedWithEvent(eventService)
    }
}
