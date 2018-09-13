package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.KalturaClientConfig
import com.boclips.videos.service.application.SearchVideos
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.search.ElasticSearchProperties
import com.boclips.videos.service.infrastructure.search.ElasticSearchResultConverter
import com.boclips.videos.service.infrastructure.search.ElasticSearchService
import com.boclips.videos.service.infrastructure.search.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig(val objectMapper: ObjectMapper) {

    @Bean
    fun searchService(elasticSearchProperties: ElasticSearchProperties): SearchService {
        return ElasticSearchService(searchHitConverter(), elasticSearchProperties)
    }

    @Bean
    fun videoService(searchService: SearchService, eventService: EventService, kalturaClient: KalturaClient): VideoService {
        return DefaultVideoService(searchService = searchService, eventService = eventService, kalturaClient = kalturaClient)
    }

    @Bean
    fun searchHitConverter() = ElasticSearchResultConverter(objectMapper)

    @Bean
    fun searchVideos(videoService: VideoService) = SearchVideos(videoService)

    @Bean
    fun eventService(eventLogRepository: EventLogRepository) = EventService(eventLogRepository)

    @Bean
    fun kalturaClient(kalturaClientProperties: KalturaClientProperties): KalturaClient = KalturaClient.create(KalturaClientConfig.builder()
            .partnerId(kalturaClientProperties.partnerId)
            .userId(kalturaClientProperties.userId)
            .secret(kalturaClientProperties.secret)
            .build())
}