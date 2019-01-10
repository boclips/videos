package com.boclips.videos.service.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsContext {
    @Bean
    fun videoCounter(registry: MeterRegistry) = registry.counter("video_count")
}