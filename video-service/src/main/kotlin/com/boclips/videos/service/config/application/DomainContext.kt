package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestrictionsRepository
import com.boclips.contentpartner.service.infrastructure.agerange.MongoAgeRangeRepository
import com.boclips.contentpartner.service.infrastructure.contentpartner.MongoContentPartnerRepository
import com.boclips.contentpartner.service.infrastructure.contentpartnercontract.ContentPartnerContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contentpartnercontract.MongoContentPartnerContractRepository
import com.boclips.contentpartner.service.infrastructure.legalrestriction.MongoLegalRestrictionsRepository
import com.boclips.contentpartner.service.infrastructure.newlegalrestriction.MongoNewLegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionCreationService
import com.boclips.videos.service.domain.service.collection.CollectionReadService
import com.boclips.videos.service.domain.service.collection.CollectionRepositoryEventsDecorator
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
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
    private val mongoSubjectRepository: MongoSubjectRepository
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
    fun collectionReadService(
        collectionRepository: CollectionRepository,
        collectionSearchService: CollectionSearchService,
        collectionAccessService: CollectionAccessService,
        eventService: EventService,
        videoService: VideoService
    ): CollectionReadService {
        return CollectionReadService(
            collectionRepository,
            collectionSearchService,
            collectionAccessService,
            eventService,
            videoService
        )
    }

    @Bean
    fun collectionWriteService(
        collectionRepository: CollectionRepository,
        collectionReadService: CollectionReadService
    ): CollectionCreationService {
        return CollectionCreationService(collectionRepository, collectionReadService)
    }

    @Bean
    fun collectionAccessService(userService: UserService): CollectionAccessService {
        return CollectionAccessService(userService)
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
        return MongoContentPartnerRepository(
            mongoClient
        )
    }

    @Bean
    fun contentPartnerContractRepository(
        converter: ContentPartnerContractDocumentConverter
    ): ContentPartnerContractRepository {
        return MongoContentPartnerContractRepository(
            mongoClient,
            converter
        )
    }

    @Bean
    fun contentPartnerContractDocumentConverter(): ContentPartnerContractDocumentConverter {
        return ContentPartnerContractDocumentConverter()
    }

    @Bean
    fun ageRangeRepository(): AgeRangeRepository {
        return MongoAgeRangeRepository(
            mongoClient
        )
    }

    @Bean
    fun legalRestrictionsRepository(): LegalRestrictionsRepository {
        return MongoLegalRestrictionsRepository(
            mongoClient
        )
    }

    @Bean
    fun newLegalRestrictionsRepository(): NewLegalRestrictionsRepository {
        return MongoNewLegalRestrictionsRepository(mongoClient)
    }

    @Bean
    fun userService(usersClient: UsersClient, organisationsClient: OrganisationsClient): UserService {
        return ApiUserService(usersClient, organisationsClient)
    }

    @Bean
    fun eventService(): EventService = EventService(eventBus)
}
