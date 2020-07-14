package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.contentpartner.service.domain.service.channel.ChannelRepositoryEventDecorator
import com.boclips.contentpartner.service.domain.service.contract.ContractRepositoryEventDecorator
import com.boclips.contentpartner.service.infrastructure.agerange.MongoAgeRangeRepository
import com.boclips.contentpartner.service.infrastructure.channel.MongoChannelRepository
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contract.MongoContractRepository
import com.boclips.contentpartner.service.infrastructure.contract.legalrestrictions.MongoContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.infrastructure.legalrestriction.MongoLegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.collection.*
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepositoryEventDecorator
import com.boclips.videos.service.domain.service.subject.SubjectService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.*
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.boclips.videos.service.domain.service.video.plackback.PlaybackUpdateService
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.subject.MongoSubjectRepository
import com.boclips.videos.service.infrastructure.user.ApiUserService
import com.boclips.videos.service.infrastructure.video.MongoVideoRepository
import com.mongodb.MongoClient
import org.springframework.boot.web.client.RestTemplateBuilder
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
        channelRepository: ChannelRepository,
        videoRepository: VideoRepository,
        videoIndex: VideoIndex,
        playbackRepository: PlaybackRepository,
        batchProcessingConfig: BatchProcessingConfig
    ): VideoRetrievalService {
        return VideoRetrievalService(videoRepository, videoIndex)
    }

    @Bean
    fun videoDuplicationHandler(
        videoRepository: VideoRepository,
        collectionRepository: CollectionRepository
    ): VideoDuplicationService {
        return VideoDuplicationService(videoRepository, collectionRepository)
    }

    @Bean
    fun videoCreationService(
        channelRepository: ChannelRepository,
        videoRepository: VideoRepository,
        videoDuplicationService: VideoDuplicationService
    ): VideoCreationService {
        return VideoCreationService(channelRepository, videoRepository, videoDuplicationService)
    }

    @Bean
    fun videoDeletionService(
        channelRepository: ChannelRepository,
        videoRepository: VideoRepository,
        collectionRepository: CollectionRepository,
        videoIndex: VideoIndex,
        playbackRepository: PlaybackRepository
    ): VideoDeletionService {
        return VideoDeletionService(videoRepository, collectionRepository, videoIndex, playbackRepository)
    }


    @Bean
    fun collectionRetrievalService(
        collectionRepository: CollectionRepository,
        collectionIndex: CollectionIndex,
        collectionAccessService: CollectionAccessService,
        eventService: EventService,
        videoRetrievalService: VideoRetrievalService
    ): CollectionRetrievalService {
        return CollectionRetrievalService(
            collectionRepository,
            collectionIndex,
            collectionAccessService,
            eventService,
            videoRetrievalService
        )
    }

    @Bean
    fun collectionCreationService(
        collectionRepository: CollectionRepository,
        collectionIndex: CollectionIndex,
        collectionRetrievalService: CollectionRetrievalService
    ): CollectionCreationService {
        return CollectionCreationService(collectionRepository, collectionIndex, collectionRetrievalService)
    }

    @Bean
    fun collectionDeletionService(
        collectionRepository: CollectionRepository,
        collectionRetrievalService: CollectionRetrievalService,
        collectionIndex: CollectionIndex
    ): CollectionDeletionService {
        return CollectionDeletionService(collectionRepository, collectionIndex, collectionRetrievalService)
    }

    @Bean
    fun collectionUpdateService(
        collectionRepository: CollectionRepository,
        collectionRetrievalService: CollectionRetrievalService,
        collectionIndex: CollectionIndex
    ): CollectionUpdateService {
        return CollectionUpdateService(collectionRepository, collectionRetrievalService, collectionIndex)
    }

    @Bean
    fun collectionBookmarkService(
        collectionRepository: CollectionRepository,
        collectionRetrievalService: CollectionRetrievalService,
        collectionIndex: CollectionIndex
    ): CollectionBookmarkService {
        return CollectionBookmarkService(collectionRetrievalService, collectionIndex, collectionRepository)
    }

    @Bean
    fun collectionAccessService(): CollectionAccessService {
        return CollectionAccessService()
    }

    @Bean
    fun videoRepository(batchProcessingConfig: BatchProcessingConfig): VideoRepository {
        return VideoRepositoryEventDecorator(
            MongoVideoRepository(
                mongoClient,
                batchProcessingConfig = batchProcessingConfig
            ), eventBus, batchProcessingConfig = batchProcessingConfig
        )
    }

    @Primary
    @Bean
    fun subjectRepository(): SubjectRepository {
        return SubjectRepositoryEventDecorator(mongoSubjectRepository, eventBus)
    }

    @Bean
    fun subjectService(
        subjectRepository: SubjectRepository,
        collectionRepository: CollectionRepository,
        videoRepository: VideoRepository,
        collectionIndex: CollectionIndex
    ): SubjectService {
        return SubjectService(subjectRepository, videoRepository, collectionRepository)
    }

    @Bean
    fun playbackRepository(kalturaPlaybackProvider: PlaybackProvider, youtubePlaybackProvider: PlaybackProvider)
        : PlaybackRepository {
        return PlaybackRepository(kalturaPlaybackProvider, youtubePlaybackProvider)
    }

    @Bean
    fun playbackService(
        videoRepository: VideoRepository,
        playbackRepository: PlaybackRepository
    ): PlaybackUpdateService {
        return PlaybackUpdateService(videoRepository, playbackRepository)
    }

    @Bean
    fun kalturaPlaybackProvider(
        kalturaClient: KalturaClient,
        restTemplateBuilder: RestTemplateBuilder
    ): PlaybackProvider {
        return KalturaPlaybackProvider(kalturaClient, restTemplateBuilder)
    }

    @Bean
    @Profile("!fakes")
    fun youtubePlaybackProvider(youtubeProperties: YoutubeProperties): PlaybackProvider {
        return YoutubePlaybackProvider(youtubeProperties.apiKey)
    }

    @Bean
    fun contentPartnerRepository(): ChannelRepository {
        return ChannelRepositoryEventDecorator(
            channelRepository = MongoChannelRepository(mongoClient = mongoClient),
            eventBus = eventBus,
            eventConverter = EventConverter(),
            subjectRepository = mongoSubjectRepository
        )
    }

    @Bean
    fun contentPartnerContractRepository(
        converter: ContractDocumentConverter
    ): ContractRepository {
        return ContractRepositoryEventDecorator(
            MongoContractRepository(
                mongoClient,
                converter
            ),
            EventConverter(),
            eventBus
        )
    }

    @Bean
    fun contentPartnerContractDocumentConverter(): ContractDocumentConverter {
        return ContractDocumentConverter()
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
    fun contractLegalRestrictionsRepository(): ContractLegalRestrictionsRepository {
        return MongoContractLegalRestrictionsRepository(
            mongoClient
        )
    }

    @Bean
    fun userService(usersClient: UsersClient, organisationsClient: OrganisationsClient): UserService {
        return ApiUserService(usersClient, organisationsClient)
    }

    @Bean
    fun eventService(): EventService = EventService(eventBus)
}
