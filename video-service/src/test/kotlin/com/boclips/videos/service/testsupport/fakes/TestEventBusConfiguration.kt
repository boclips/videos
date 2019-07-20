package com.boclips.videos.service.testsupport.fakes

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestEventBusConfiguration {

    @Bean
    fun testEventBus(): EventBus {
        return SynchronousFakeEventBus()
    }
}
