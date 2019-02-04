package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.collection.AddVideoToDefaultCollection
import com.boclips.videos.service.application.collection.GetDefaultCollection
import com.boclips.videos.service.application.collection.RemoveVideoFromDefaultCollection
import com.boclips.videos.service.application.video.*
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.collection.mongo.CollectionDocumentConverter
import com.boclips.videos.service.infrastructure.collection.mongo.MongoCollectionService
import com.boclips.videos.service.infrastructure.collection.mysql.CollectionEntityRepository
import com.boclips.videos.service.infrastructure.collection.mysql.MySqlCollectionService
import com.boclips.videos.service.infrastructure.collection.mysql.VideoInCollectionEntityRepository
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoAssetRepository
import com.boclips.videos.service.presentation.video.CreateVideoRequestToAssetConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.mongodb.MongoClient
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors


@Configuration
class ApplicationContext {

    @Bean
    fun getVideoById(searchService: SearchService, videoService: VideoService, videoAssetRepository: VideoAssetRepository) =
            GetVideoById(
                    videoService = videoService,
                    videoToResourceConverter = VideoToResourceConverter(),
                    videoAssetRepository = videoAssetRepository
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
    fun createVideo(videoAssetRepository: VideoAssetRepository, getVideoById: GetVideoById, searchService: SearchService, playbackRepository: PlaybackRespository, videoCounter: Counter, legacySearchService: LegacySearchService): CreateVideo {
        return CreateVideo(videoAssetRepository, getVideoById, CreateVideoRequestToAssetConverter(), searchService, playbackRepository, videoCounter, legacySearchService)
    }

    @Bean
    fun patchVideo(videoService: VideoService): PatchVideo {
        return PatchVideo(videoService)
    }

    @Bean
    fun getAllVideosById(videoService: VideoService): GetAllVideosById {
        return GetAllVideosById(videoService, VideoToResourceConverter())
    }

    @Bean
    fun bulkUpdate(videoAssetRepository: VideoAssetRepository, searchService: SearchService, legacySearchService: LegacySearchService): BulkUpdate {
        return BulkUpdate(videoAssetRepository, searchService, legacySearchService)
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
    @Primary
    fun collectionService(mongoClient: MongoClient,
                          videoInCollectionEntityRepository: VideoInCollectionEntityRepository,
                          videoService: VideoService): CollectionService {
        return MongoCollectionService(mongoClient, CollectionDocumentConverter(), videoService)
    }

    @Bean
    fun mysqlCollectionService(collectionEntityRepository: CollectionEntityRepository,
                               videoInCollectionEntityRepository: VideoInCollectionEntityRepository,
                               videoService: VideoService,
                               mongoVideoAssetRepository: MongoVideoAssetRepository): MySqlCollectionService {
        return MySqlCollectionService(collectionEntityRepository, videoInCollectionEntityRepository, videoService, mongoVideoAssetRepository)
    }

    @Bean
    fun mongoVideoRepository(
            mongoClient: MongoClient
    ): MongoVideoAssetRepository {
        return MongoVideoAssetRepository(mongoClient)
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
    fun buildLegacySearchIndex(videoAssetRepository: VideoAssetRepository,
                               legacySearchService: LegacySearchService
    ): BuildLegacySearchIndex {
        return BuildLegacySearchIndex(
                videoAssetRepository = videoAssetRepository,
                legacySearchService = legacySearchService
        )
    }

    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(
                Executors.newFixedThreadPool(3))
    }

    @Bean
    fun getDefaultCollection(collectionService: CollectionService): GetDefaultCollection {
        return GetDefaultCollection(collectionService, VideoToResourceConverter())
    }

    @Bean
    fun addVideoToDefaultCollection(collectionService: CollectionService, eventService: EventService): AddVideoToDefaultCollection {
        return AddVideoToDefaultCollection(collectionService, eventService)
    }

    @Bean
    fun removeVideoFromDefaultCollection(collectionService: CollectionService, eventService: EventService): RemoveVideoFromDefaultCollection {
        return RemoveVideoFromDefaultCollection(collectionService, eventService)
    }
}
