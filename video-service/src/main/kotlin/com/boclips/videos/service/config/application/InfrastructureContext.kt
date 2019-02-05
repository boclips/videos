package com.boclips.videos.service.config.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors

@Configuration
class InfrastructureContext {
    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(Executors.newFixedThreadPool(3))
    }
}