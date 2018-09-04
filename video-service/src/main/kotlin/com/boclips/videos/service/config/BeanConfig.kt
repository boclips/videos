package com.boclips.videos.service.config

import com.boclips.videos.service.application.SearchVideos
import com.boclips.videos.service.domain.service.SearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {
    @Bean
    fun searchVideos(searchService: SearchService): SearchVideos {
        return SearchVideos(searchService)
    }
}