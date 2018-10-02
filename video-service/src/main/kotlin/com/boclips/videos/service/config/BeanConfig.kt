package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.KalturaClientConfig
import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.application.event.GetLatestInteractions
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideos
import com.boclips.videos.service.domain.service.PlaybackService
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventMonitoringConfig
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.RequestId
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackService
import com.boclips.videos.service.infrastructure.search.ElasticSearchResultConverter
import com.boclips.videos.service.infrastructure.search.ElasticSearchService
import com.boclips.videos.service.infrastructure.video.MysqlVideoService
import com.boclips.videos.service.infrastructure.video.VideoRepository
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
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
    fun getVideos(searchService: SearchService,
                  videoService: VideoService,
                  requestId: RequestId,
                  playbackService: PlaybackService) =
            GetVideos(
                    videoService = videoService,
                    videoToResourceConverter = VideoToResourceConverter(),
                    playbackService = playbackService,
                    requestId = requestId
            )

    @Bean
    fun deleteVideos(videoService: VideoService): DeleteVideos {
        return DeleteVideos(videoService)
    }

    @Bean
    fun videoService(searchService: SearchService, videoRepository: VideoRepository): VideoService {
        return MysqlVideoService(searchService = searchService, videoRepository = videoRepository)
    }

    @Bean
    fun playbackService(kalturaClient: KalturaClient): PlaybackService {
        return KalturaPlaybackService(kalturaClient = kalturaClient)
    }

    @Bean
    fun eventService(eventLogRepository: EventLogRepository,
                     eventMonitoringConfig: EventMonitoringConfig,
                     mongoTemplate: MongoTemplate) =
            EventService(
                    eventLogRepository = eventLogRepository,
                    eventMonitoringConfig = eventMonitoringConfig,
                    mongoTemplate = mongoTemplate)

    @Bean
    fun searchService(propertiesElasticSearch: PropertiesElasticSearch, eventService: EventService, kalturaClient: KalturaClient, requestId: RequestId): SearchService {
        return ElasticSearchService(
                elasticSearchResultConverter = searchHitConverter(),
                eventService = eventService,
                requestId = requestId,
                propertiesElasticSearch = propertiesElasticSearch)
    }

    @Bean
    fun createEvent(eventService: EventService): CreateEvent {
        return CreateEvent(
                eventService = eventService
        )
    }

    @Bean
    fun checkEventsStatus(eventService: EventService): CheckEventsStatus {
        return CheckEventsStatus(
                eventService = eventService
        )
    }

    @Bean
    fun searchHitConverter() = ElasticSearchResultConverter(
            objectMapper = objectMapper
    )

    @Bean
    fun kalturaClient(propertiesKaltura: PropertiesKaltura): KalturaClient = KalturaClient.create(KalturaClientConfig.builder()
            .partnerId(propertiesKaltura.partnerId)
            .userId(propertiesKaltura.userId)
            .secret(propertiesKaltura.secret)
            .build())

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    fun requestId(): RequestId {
        return RequestId()
    }

    @Bean
    fun getLatestInteractions(eventService: EventService): GetLatestInteractions {
        return GetLatestInteractions(eventService = eventService)
    }
}
