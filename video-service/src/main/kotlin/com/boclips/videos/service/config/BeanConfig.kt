package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.KalturaClientConfig
import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.infrastructure.elastic.ElasticSearchConfig
import com.boclips.search.service.infrastructure.elastic.ElasticSearchService
import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.application.event.GetLatestInteractions
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideos
import com.boclips.videos.service.domain.service.PlaybackService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventMonitoringConfig
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackService
import com.boclips.videos.service.infrastructure.video.MysqlVideoService
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


@Configuration
class BeanConfig {
    @Bean
    fun getVideos(searchService: SearchService,
                  videoService: VideoService,
                  playbackService: PlaybackService) =
            GetVideos(
                    videoService = videoService,
                    videoToResourceConverter = VideoToResourceConverter(),
                    playbackService = playbackService
            )

    @Bean
    fun deleteVideos(videoService: VideoService): DeleteVideos {
        return DeleteVideos(videoService)
    }

    @Bean
    fun videoService(searchService: SearchService,
                     jdbcTemplate: NamedParameterJdbcTemplate,
                     playbackService: PlaybackService): VideoService {
        return MysqlVideoService(
                searchService = searchService,
                jdbcTemplate = jdbcTemplate,
                playbackVideo = playbackService)
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
    @Profile("!fake-search")
    fun searchService(propertiesElasticSearch: PropertiesElasticSearch, kalturaClient: KalturaClient): SearchService {
        return ElasticSearchService(ElasticSearchConfig(
                host = propertiesElasticSearch.host,
                port = propertiesElasticSearch.port,
                username = propertiesElasticSearch.username,
                password = propertiesElasticSearch.password
        ))
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
    fun kalturaClient(propertiesKaltura: PropertiesKaltura): KalturaClient = KalturaClient.create(KalturaClientConfig.builder()
            .partnerId(propertiesKaltura.partnerId)
            .userId(propertiesKaltura.userId)
            .secret(propertiesKaltura.secret)
            .build())

    @Bean
    fun getLatestInteractions(eventService: EventService): GetLatestInteractions {
        return GetLatestInteractions(eventService = eventService)
    }
}
