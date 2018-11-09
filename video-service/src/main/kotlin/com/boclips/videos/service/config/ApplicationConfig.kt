package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.application.event.GetEvent
import com.boclips.videos.service.application.event.GetLatestInteractions
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideoById
import com.boclips.videos.service.application.video.GetVideosByQuery
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.boclips.videos.service.domain.service.PlaybackService
import com.boclips.videos.service.domain.service.VideoRepository
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.domain.service.filters.TeacherContentFilter
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventMonitoringConfig
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.video.MysqlVideoRepository
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.task.TaskExecutor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors


@Configuration
class ApplicationConfig {
    @Bean
    fun getVideoById(searchService: SearchService, videoService: VideoService) =
            GetVideoById(
                    videoService = videoService,
                    videoToResourceConverter = VideoToResourceConverter()
            )

    @Bean
    fun getVideosByQuery(searchService: SearchService,
                         videoService: VideoService,
                         playbackService: PlaybackService) =
            GetVideosByQuery(
                    videoService = videoService,
                    videoToResourceConverter = VideoToResourceConverter()
            )

    @Bean
    fun deleteVideos(videoService: VideoService): DeleteVideos {
        return DeleteVideos(videoService)
    }

    @Bean
    fun videoService(
            videoRepository: VideoRepository,
            searchService: SearchService,
            playbackService: PlaybackService
    ): VideoService {
        return VideoService(
                videoRepository = videoRepository,
                searchService = searchService,
                playbackService = playbackService
        )
    }

    @Bean
    fun videoRepository(jdbcTemplate: NamedParameterJdbcTemplate): VideoRepository {
        return MysqlVideoRepository(jdbcTemplate)
    }

    @Bean
    fun playbackService(kalturaPlaybackProvider: PlaybackProvider, youtubePlaybackProvider: PlaybackProvider): PlaybackService {
        return PlaybackService(kalturaPlaybackProvider = kalturaPlaybackProvider, youtubePlaybackProvider = youtubePlaybackProvider)
    }

    @Bean
    fun kalturaPlaybackProvider(kalturaClient: KalturaClient): PlaybackProvider {
        return KalturaPlaybackProvider(kalturaClient = kalturaClient)
    }

    @Bean
    @Profile("!fakes")
    fun youtubePlaybackProvider(youtubeProperties: YoutubeProperties): PlaybackProvider {
        return YoutubePlaybackProvider(youtubeProperties.apiKey)
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
    fun createEvent(eventService: EventService, emailClient: EmailClient): CreateEvent {
        return CreateEvent(
                eventService = eventService,
                emailClient = emailClient
        )
    }

    @Bean
    fun getEvent(eventService: EventService): GetEvent {
        return GetEvent(eventService = eventService)
    }

    @Bean
    fun checkEventsStatus(eventService: EventService): CheckEventsStatus {
        return CheckEventsStatus(
                eventService = eventService
        )
    }

    @Bean
    fun getLatestInteractions(eventService: EventService): GetLatestInteractions {
        return GetLatestInteractions(eventService = eventService)
    }

    @Bean
    fun rebuildSearchIndex(videoRepository: VideoRepository, searchService: com.boclips.search.service.domain.SearchService, teacherContentFilter: TeacherContentFilter): RebuildSearchIndex {
        return RebuildSearchIndex(
                videoRepository = videoRepository,
                searchService = searchService,
                teacherContentFilter = teacherContentFilter
        )
    }

    @Bean
    fun teacherContentFilter(): TeacherContentFilter {
        return TeacherContentFilter()
    }

    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(
                Executors.newFixedThreadPool(3))
    }
}
