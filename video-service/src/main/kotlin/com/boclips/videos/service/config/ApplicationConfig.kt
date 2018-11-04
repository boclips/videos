package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.application.event.GetEvent
import com.boclips.videos.service.application.event.GetLatestInteractions
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.GetVideos
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.domain.service.PlaybackService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.domain.service.filters.TeacherContentFilter
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.EventMonitoringConfig
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackService
import com.boclips.videos.service.infrastructure.video.MysqlVideoService
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors


@Configuration
class ApplicationConfig {
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
                playbackVideo = playbackService
        )
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
    fun rebuildSearchIndex(videoService: VideoService, searchService: com.boclips.search.service.domain.SearchService, teacherContentFilter: TeacherContentFilter): RebuildSearchIndex {
        return RebuildSearchIndex(
                videoService = videoService,
                searchService = searchService,
                teacherContentFilter = teacherContentFilter)
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
