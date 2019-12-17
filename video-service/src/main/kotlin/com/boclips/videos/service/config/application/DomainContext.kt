package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.infrastructure.MongoContentPartnerRepository
import com.boclips.contentpartner.service.infrastructure.MongoLegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionRepositoryEventsDecorator
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepositoryEventDecorator
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import com.boclips.videos.service.domain.service.video.VideoRepositoryEventDecorator
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.subject.MongoSubjectRepository
import com.boclips.videos.service.infrastructure.user.ApiUserService
import com.boclips.videos.service.infrastructure.video.MongoVideoRepository
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class DomainContext(
    private val mongoClient: MongoClient,
    private val eventBus: EventBus,
    private val mongoCollectionRepository: MongoCollectionRepository,
    private val mongoSubjectRepository: MongoSubjectRepository,
    private val userServiceClient: UserServiceClient
) {

    @Bean
    fun videoService(
        contentPartnerRepository: ContentPartnerRepository,
        videoRepository: VideoRepository,
        videoSearchService: VideoSearchService,
        playbackRepository: PlaybackRepository,
        batchProcessingConfig: BatchProcessingConfig
    ): VideoService {
        return VideoService(contentPartnerRepository, videoRepository, videoSearchService)
    }

    @Bean
    fun collectionService(
        collectionRepository: CollectionRepository,
        collectionSearchService: CollectionSearchService,
        collectionAccessService: CollectionAccessService
    ): CollectionService {
        return CollectionService(collectionRepository, collectionSearchService, collectionAccessService)
    }

    @Bean
    fun collectionAccessService(): CollectionAccessService {
        return CollectionAccessService()
    }

    @Primary
    @Bean
    fun collectionRepository(eventService: EventService): CollectionRepository {
        return CollectionRepositoryEventsDecorator(mongoCollectionRepository, eventService)
    }

    @Bean
    fun videoRepository(batchProcessingConfig: BatchProcessingConfig): VideoRepository {
        return VideoRepositoryEventDecorator(
            MongoVideoRepository(
                mongoClient,
                batchProcessingConfig = batchProcessingConfig
            ), eventBus
        )
    }

    @Primary
    @Bean
    fun subjectRepository(): SubjectRepository {
        return SubjectRepositoryEventDecorator(mongoSubjectRepository, eventBus)
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

    @Bean
    fun legalRestrictionsRepository(): LegalRestrictionsRepository {
        return MongoLegalRestrictionsRepository(mongoClient)
    }

    @Bean
    fun userService(): UserService {
        return ApiUserService(userServiceClient)
    }

    @Bean
    fun eventService(): EventService = EventService(eventBus)
}
