package com.boclips.videos.service.config

import com.boclips.videos.service.application.SearchVideos
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.search.ElasticSearchProperties
import com.boclips.videos.service.infrastructure.search.ElasticSearchService
import com.boclips.videos.service.infrastructure.search.EventLoggingSearchService
import com.boclips.videos.service.infrastructure.search.SearchHitConverter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig(val objectMapper: ObjectMapper) {

    @Bean
    fun searchService(elasticSearchProperties: ElasticSearchProperties, eventService: EventService): SearchService {
        val searchService = ElasticSearchService(searchHitConverter(), elasticSearchProperties)
        return EventLoggingSearchService(searchService, eventService)
    }

    @Bean
    fun searchHitConverter() = SearchHitConverter(objectMapper)

    @Bean
    fun searchVideos(searchService: SearchService) = SearchVideos(searchService)

    @Bean
    fun eventService(eventLogRepository: EventLogRepository) = EventService(eventLogRepository)
}