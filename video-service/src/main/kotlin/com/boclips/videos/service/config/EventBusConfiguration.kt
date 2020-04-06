package com.boclips.videos.service.config

import com.boclips.eventbus.EventBus
import com.boclips.videos.service.domain.service.events.NullEventBus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class EventBusConfiguration {

    @Bean
    @Profile("no-event-bus")
    fun eventBus(): EventBus {
        return NullEventBus()
    }
}
