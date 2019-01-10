package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.application.video.*
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.video.MysqlVideoAssetRepository
import com.boclips.videos.service.infrastructure.video.VideoEntityRepository
import com.boclips.videos.service.infrastructure.video.subject.SubjectRepository
import com.boclips.videos.service.presentation.video.CreateVideoRequestToAssetConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors


@Configuration
class ApplicationContext {
    @Bean
    fun getVideoById(searchService: SearchService, videoService: VideoService) =
            GetVideoById(
                    videoService = videoService,
                    videoToResourceConverter = VideoToResourceConverter()
            )

    @Bean
    fun getVideosByQuery(searchService: SearchService,
                         videoService: VideoService,
                         playbackRespository: PlaybackRespository) =
            GetVideosByQuery(
                    videoService = videoService,
                    videoToResourceConverter = VideoToResourceConverter()
            )

    @Bean
    fun createVideo(videoAssetRepository: VideoAssetRepository, getVideoById: GetVideoById, searchService: SearchService, playbackRepository: PlaybackRespository, videoCounter: Counter): CreateVideo {
        return CreateVideo(videoAssetRepository, getVideoById, CreateVideoRequestToAssetConverter(), searchService, playbackRepository, videoCounter)
    }

    @Bean
    fun patchVideo(videoService: VideoService): PatchVideo {
        return PatchVideo(videoService)
    }

    @Bean
    fun deleteVideos(playbackRespository: PlaybackRespository,
                     searchService: SearchService,
                     videoAssetRepository: VideoAssetRepository
    ): DeleteVideos {
        return DeleteVideos(videoAssetRepository = videoAssetRepository,
                searchService = searchService,
                playbackRepository = playbackRespository)
    }

    @Bean
    fun videoService(
            videoAssetRepository: VideoAssetRepository,
            searchService: SearchService,
            playbackRespository: PlaybackRespository
    ): VideoService {
        return VideoService(
                videoAssetRepository = videoAssetRepository,
                searchService = searchService,
                playbackRepository = playbackRespository
        )
    }

    @Bean
    fun videoRepository(
            subjectRepository: SubjectRepository,
            videoEntityRepository: VideoEntityRepository
    ): VideoAssetRepository {
        return MysqlVideoAssetRepository(subjectRepository, videoEntityRepository)
    }

    @Bean
    fun playbackRepository(kalturaPlaybackProvider: PlaybackProvider, youtubePlaybackProvider: PlaybackProvider): PlaybackRespository {
        return PlaybackRespository(kalturaPlaybackProvider = kalturaPlaybackProvider, youtubePlaybackProvider = youtubePlaybackProvider)
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
    fun rebuildSearchIndex(videoAssetRepository: VideoAssetRepository,
                           searchService: SearchService
    ): RebuildSearchIndex {
        return RebuildSearchIndex(
                videoAssetRepository = videoAssetRepository,
                searchService = searchService
        )
    }

    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(
                Executors.newFixedThreadPool(3))
    }
}
