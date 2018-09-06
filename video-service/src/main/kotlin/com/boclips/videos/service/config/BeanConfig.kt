package com.boclips.videos.service.config

import com.boclips.videos.service.application.SearchVideos
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.search.ElasticSearchProperties
import com.boclips.videos.service.infrastructure.search.ElasticSearchService
import com.boclips.videos.service.infrastructure.search.EventLoggingSearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {

    @Bean
    fun searchService(elasticSearchProperties: ElasticSearchProperties, eventService: EventService): SearchService {
        val searchService = ElasticSearchService(elasticSearchProperties)
        return EventLoggingSearchService(searchService, eventService)
    }

    @Bean
    fun searchVideos(searchService: SearchService): SearchVideos {
        return SearchVideos(searchService)
    }

    @Bean
    fun eventService(eventLogRepository: EventLogRepository): EventService {
        return EventService(eventLogRepository)
    }
}