package com.boclips.videos.service.config.application

import com.boclips.kalturaclient.KalturaClient
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.video.BuildLegacySearchIndex
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.infrastructure.collection.CollectionDocumentConverter
import com.boclips.videos.service.infrastructure.collection.MongoCollectionService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoAssetRepository
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
class DomainContext(val mongoClient: MongoClient) {

    @Bean
    fun videoService(
            videoAssetRepository: VideoAssetRepository,
            searchService: SearchService,
            playbackRespository: PlaybackRespository
    ): VideoService {
        return VideoService(videoAssetRepository, searchService, playbackRespository)
    }

    @Bean
    fun collectionService(videoService: VideoService): CollectionService {
        return MongoCollectionService(mongoClient, CollectionDocumentConverter(), videoService)
    }

    @Bean
    fun videoRepository(): VideoAssetRepository {
        return MongoVideoAssetRepository(mongoClient)
    }

    @Bean
    fun playbackRepository(kalturaPlaybackProvider: PlaybackProvider, youtubePlaybackProvider: PlaybackProvider)
            : PlaybackRespository {
        return PlaybackRespository(kalturaPlaybackProvider, youtubePlaybackProvider)
    }

    @Bean
    fun kalturaPlaybackProvider(kalturaClient: KalturaClient): PlaybackProvider {
        return KalturaPlaybackProvider(kalturaClient)
    }

    @Bean
    @Profile("!fakes")
    fun youtubePlaybackProvider(youtubeProperties: YoutubeProperties): PlaybackProvider {
        return YoutubePlaybackProvider(youtubeProperties.apiKey)
    }
}
