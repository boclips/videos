package com.boclips.videos.service.config.application

import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.subject.EventPublishingSubjectRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.EventPublishingVideoRepository
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.collection.MongoCollectionFilterContractAdapter
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.infrastructure.contentPartner.MongoContentPartnerRepository
import com.boclips.videos.service.infrastructure.discipline.MongoDisciplineRepository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.subject.MongoSubjectRepository
import com.boclips.videos.service.infrastructure.tag.MongoTagRepository
import com.boclips.videos.service.infrastructure.video.MongoVideoRepository
import com.mongodb.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DomainContext(
    val mongoClient: MongoClient,
    val eventBus: EventBus,
    val mongoCollectionFilterContractAdapter: MongoCollectionFilterContractAdapter
) {

    @Bean
    fun videoService(
        contentPartnerRepository: ContentPartnerRepository,
        videoRepository: VideoRepository,
        videoSearchService: VideoSearchService,
        playbackRepository: PlaybackRepository
    ): VideoService {
        return VideoService(
            contentPartnerRepository,
            videoRepository,
            videoSearchService
        )
    }

    @Bean
    fun collectionService(
        collectionRepository: CollectionRepository,
        collectionSearchService: CollectionSearchService
    ): CollectionService {
        return CollectionService(
            collectionRepository,
            collectionSearchService
        )
    }

    @Bean
    fun collectionRepository(): CollectionRepository {
        return MongoCollectionRepository(
            mongoClient = mongoClient,
            mongoCollectionFilterContractAdapter = mongoCollectionFilterContractAdapter
        )
    }

    @Bean
    fun videoRepository(): VideoRepository {
        return EventPublishingVideoRepository(
            MongoVideoRepository(
                mongoClient
            ), eventBus
        )
    }

    @Bean
    fun subjectRepository(): SubjectRepository {
        return EventPublishingSubjectRepository(MongoSubjectRepository(mongoClient), eventBus)
    }

    @Bean
    fun tagRepository(): TagRepository {
        return MongoTagRepository(mongoClient)
    }

    @Bean
    fun disciplineRepository(): DisciplineRepository {
        return MongoDisciplineRepository(mongoClient, subjectRepository())
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
