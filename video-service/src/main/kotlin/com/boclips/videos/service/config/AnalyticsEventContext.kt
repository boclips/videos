package com.boclips.videos.service.config

import com.boclips.videos.service.application.analytics.SaveCollectionInteractedWithEvent
import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.application.analytics.SavePlayerInteractedWithEvent
import com.boclips.videos.service.application.analytics.SaveSearchQuerySuggestionsCompletedEvent
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

    @Bean
    fun saveCollectionInteractedWithEvent(): SaveCollectionInteractedWithEvent {
        return SaveCollectionInteractedWithEvent(eventService)
    }

    @Bean
    fun saveSearchSuggestionsCompletedEvent(): SaveSearchQuerySuggestionsCompletedEvent {
        return SaveSearchQuerySuggestionsCompletedEvent(eventService)
    }
}
