package com.boclips.videos.service.config.application


import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.application.video.ClassifyVideo
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.config.properties.YoutubeProperties
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.infrastructure.contentPartner.MongoContentPartnerRepository
import com.boclips.videos.service.infrastructure.discipline.MongoDisciplineRepository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.subject.MongoSubjectRepository
import com.boclips.videos.service.infrastructure.tag.MongoTagRepository
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoRepository
import com.mongodb.MongoClient
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DomainContext(val mongoClient: MongoClient) {

    @Bean
    fun videoService(
        contentPartnerRepository: ContentPartnerRepository,
        videoRepository: VideoRepository,
        videoSearchService: VideoSearchService,
        playbackRepository: PlaybackRepository,
        videoCounter: Counter,
        includeVideosInSearchForStream: IncludeVideosInSearchForStream,
        includeVideosInSearchForDownload: IncludeVideosInSearchForDownload,
        classifyVideo: ClassifyVideo
    ): VideoService {
        return VideoService(
            contentPartnerRepository,
            videoRepository,
            videoSearchService,
            videoCounter,
            includeVideosInSearchForStream,
            includeVideosInSearchForDownload,
            classifyVideo
        )
    }

    @Bean
    fun classifyVideo(eventBus: EventBus): ClassifyVideo {
        return ClassifyVideo(eventBus)
    }

    @Bean
    fun includeVideosInSearchForStream(eventBus: EventBus): IncludeVideosInSearchForStream {
        return IncludeVideosInSearchForStream(eventBus = eventBus)
    }

    @Bean
    fun includeVideosInSearchForDownload(eventBus: EventBus): IncludeVideosInSearchForDownload {
        return IncludeVideosInSearchForDownload(eventBus = eventBus)
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
    fun collectionRepository(videoService: VideoService): CollectionRepository {
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
    fun tagtRepository(): TagRepository {
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
