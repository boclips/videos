package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.KalturaClientConfig
import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.application.event.GetLatestInteractions
import com.boclips.videos.service.application.video.SearchVideos
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventMonitoringConfig
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.RequestId
import com.boclips.videos.service.infrastructure.search.ElasticSearchProperties
import com.boclips.videos.service.infrastructure.search.ElasticSearchResultConverter
import com.boclips.videos.service.infrastructure.search.ElasticSearchService
import com.boclips.videos.service.infrastructure.search.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.context.WebApplicationContext


@Configuration
class BeanConfig(val objectMapper: ObjectMapper) {

    @Bean
    fun videoService(): VideoService {
        return DefaultVideoService()
    }

    @Bean
    fun eventService(eventLogRepository: EventLogRepository,eventMonitoringConfig: EventMonitoringConfig, mongoTemplate: MongoTemplate) = EventService(eventLogRepository, eventMonitoringConfig, mongoTemplate)

    @Bean
    fun searchVideos(searchService: SearchService, requestId: RequestId) = SearchVideos(searchService, requestId)

    @Bean
    fun createEvent(eventService: EventService): CreateEvent {
        return CreateEvent(eventService)
    }

    @Bean
    fun checkEventsStatus(eventService: EventService): CheckEventsStatus {
        return CheckEventsStatus(eventService)
    }

    @Bean
    fun searchService(elasticSearchProperties: ElasticSearchProperties, eventService: EventService, kalturaClient: KalturaClient, requestId: RequestId): SearchService {
        return ElasticSearchService(searchHitConverter(), kalturaClient, eventService, requestId, elasticSearchProperties)
    }

    @Bean
    fun searchHitConverter() = ElasticSearchResultConverter(objectMapper)

    @Bean
    fun kalturaClient(kalturaClientProperties: KalturaClientProperties): KalturaClient = KalturaClient.create(KalturaClientConfig.builder()
            .partnerId(kalturaClientProperties.partnerId)
            .userId(kalturaClientProperties.userId)
            .secret(kalturaClientProperties.secret)
            .build())

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    fun requestId(): RequestId {
        return RequestId()
    }

    @Bean
    fun getLatestInteractions(eventService: EventService): GetLatestInteractions {
        return GetLatestInteractions(eventService)
    }
}
