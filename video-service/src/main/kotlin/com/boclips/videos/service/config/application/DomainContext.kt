package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.contentpartner.service.domain.service.channel.ChannelRepositoryEventDecorator
import com.boclips.contentpartner.service.domain.service.contract.ContractRepositoryEventDecorator
import com.boclips.contentpartner.service.domain.service.contract.legalrestrictions.ContractLegalRestrictionsEventDecorator
import com.boclips.contentpartner.service.infrastructure.agerange.MongoAgeRangeRepository
import com.boclips.contentpartner.service.infrastructure.channel.MongoChannelRepository
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contract.MongoContractRepository
import com.boclips.contentpartner.service.infrastructure.contract.legalrestrictions.MongoContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.infrastructure.legalrestriction.MongoLegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.users.api.httpclient.ContentPackagesClient
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.videos.service.application.accessrules.AccessRulesConverter
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.collection.CollectionBookmarkService
import com.boclips.videos.service.domain.service.collection.CollectionCreationService
import com.boclips.videos.service.domain.service.collection.CollectionDeletionService
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateService
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepositoryEventDecorator
import com.boclips.videos.service.domain.service.subject.SubjectService
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import com.boclips.videos.service.domain.service.suggestions.SubjectIndex
import com.boclips.videos.service.domain.service.suggestions.SuggestionsRetrievalService
import com.boclips.videos.service.domain.service.user.ContentPackageService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.VideoCreationService
import com.boclips.videos.service.domain.service.video.VideoDeletionService
import com.boclips.videos.service.domain.service.video.VideoDuplicationService
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoRepositoryEventDecorator
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.boclips.videos.service.domain.service.video.plackback.PlaybackUpdateService
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.infrastructure.contentpackage.ApiContentPackageService
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
    private val mongoSubjectRepository: MongoSubjectRepository,
    private val contentPackagesClient: ContentPackagesClient
) {

    @Bean
    fun videoDuplicationHandler(
        videoRepository: VideoRepository,
        collectionRepository: CollectionRepository,
        eventBus: EventBus
    ): VideoDuplicationService {
        return VideoDuplicationService(videoRepository, collectionRepository, eventBus)
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
        videoRepository: VideoRepository,
        collectionRepository: CollectionRepository,
        videoIndex: VideoIndex,
        playbackRepository: PlaybackRepository
    ): VideoDeletionService {
        return VideoDeletionService(videoRepository, collectionRepository, videoIndex, playbackRepository)
    }

    @Bean
    fun newSuggestionsRetrievalService(
        channelIndex: ChannelIndex,
        subjectIndex: SubjectIndex
    ): SuggestionsRetrievalService {
        return SuggestionsRetrievalService(
            channelIndex = channelIndex,
            subjectIndex = subjectIndex
        )
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
            ),
            eventBus, batchProcessingConfig = batchProcessingConfig
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
    fun playbackRepository(kalturaPlaybackProvider: PlaybackProvider, youtubePlaybackProvider: PlaybackProvider):
        PlaybackRepository {
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
        return ContractLegalRestrictionsEventDecorator(
            legalRestrictionsRepository = MongoContractLegalRestrictionsRepository(
                mongoClient
            ),
            eventBus = eventBus
        )
    }

    @Bean
    fun userService(usersClient: UsersClient, organisationsClient: OrganisationsClient): UserService {
        return ApiUserService(usersClient, organisationsClient)
    }

    @Bean
    fun contentPackageService(
        accessRulesConverter: AccessRulesConverter
    ): ContentPackageService =
        ApiContentPackageService(contentPackagesClient, accessRulesConverter)

    @Bean
    fun eventService(): EventService = EventService(eventBus)
}
