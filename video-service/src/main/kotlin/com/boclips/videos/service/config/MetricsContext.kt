package com.boclips.videos.service.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsContext {
    companion object {
        const val PREFIX = "boclips_"
    }

    @Bean
    fun videoCounter(registry: MeterRegistry) = registry.counter("${PREFIX}created_video_count")
}