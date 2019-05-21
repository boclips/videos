package com.boclips.videos.service.config.application

import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.infrastructure.contentPartner.MongoContentPartnerRepository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.subject.MongoSubjectRepository
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoRepository
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DomainContext(val mongoClient: MongoClient) {

    @Bean
    fun videoService(
        videoRepository: VideoRepository,
        searchService: SearchService,
        playbackRepository: PlaybackRepository
    ): VideoService {
        return VideoService(
            videoRepository,
            searchService
        )
    }

    @Bean
    fun videoAccessService(videoRepository: VideoRepository): VideoAccessService {
        return VideoAccessService(videoRepository = videoRepository)
    }

    @Bean
    fun collectionService(videoService: VideoService): CollectionRepository {
        return MongoCollectionRepository(mongoClient)
    }

    @Bean
    fun videoRepository(): VideoRepository {
        return MongoVideoRepository(mongoClient)
    }

    @Bean
    fun subjectRepository(): SubjectRepository {
        return MongoSubjectRepository(mongoClient)
    }

    @Bean
    fun playbackRepository(kalturaPlaybackProvider: PlaybackProvider, youtubePlaybackProvider: PlaybackProvider)
        : PlaybackRepository {
        return PlaybackRepository(kalturaPlaybackProvider, youtubePlaybackProvider)
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

    @Bean
    fun contentPartnerRepository(): ContentPartnerRepository {
        return MongoContentPartnerRepository(mongoClient)
    }
}
