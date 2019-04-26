package com.boclips.videos.service.config.application

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.application.video.AnalyseContentPartnerVideos
import com.boclips.videos.service.application.video.AnalyseVideo
import com.boclips.videos.service.application.video.BuildLegacySearchIndex
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.application.video.RefreshVideoDurations
import com.boclips.videos.service.application.video.UpdateAnalysedVideo
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.search.GetAllVideosById
import com.boclips.videos.service.application.video.search.GetVideoById
import com.boclips.videos.service.application.video.search.GetVideosByQuery
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.config.messaging.Topics
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.CreateVideoRequestToAssetConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
    val videoService: VideoService,
    val videoAssetRepository: VideoAssetRepository,
    val searchService: SearchService,
    val playbackRepository: PlaybackRepository,
    val legacySearchService: LegacySearchService,
    val collectionService: CollectionService,
    val analyticsEventService: AnalyticsEventService,
    val videoAccessService: VideoAccessService,
    val topics: Topics,
    val subjectRepository: SubjectRepository
) {

    @Bean
    fun searchVideo() = SearchVideo(
        getVideoById(),
        getAllVideosById(),
        getVideosByQuery(),
        videoAssetRepository
    )

    @Bean
    fun createVideo(
        searchVideo: SearchVideo,
        videoCounter: Counter,
        analyseVideo: AnalyseVideo
    ): CreateVideo {
        return CreateVideo(
            videoAssetRepository,
            searchVideo,
            CreateVideoRequestToAssetConverter(),
            searchService,
            playbackRepository,
            videoCounter,
            legacySearchService,
            analyseVideo
        )
    }

    @Bean
    fun updateVideo(): UpdateVideo {
        return UpdateVideo(videoAssetRepository)
    }

    @Bean
    fun updateAnalysedVideo(): UpdateAnalysedVideo {
        return UpdateAnalysedVideo(playbackRepository, videoAssetRepository)
    }

    @Bean
    fun bulkUpdate(): BulkUpdateVideo {
        return BulkUpdateVideo(videoAssetRepository, searchService, legacySearchService, videoAccessService)
    }

    @Bean
    fun deleteVideos(): DeleteVideos {
        return DeleteVideos(videoAssetRepository, searchService, playbackRepository)
    }

    @Bean
    fun createCollection(addVideoToCollection: AddVideoToCollection): CreateCollection {
        return CreateCollection(collectionService, addVideoToCollection)
    }

    @Bean
    fun getCollection(): GetCollection {
        return GetCollection(collectionService, CollectionResourceFactory(VideoToResourceConverter(), SubjectToResourceConverter(), videoService))
    }

    @Bean
    fun getPublicCollections(): GetCollections {
        return GetCollections(collectionService, CollectionResourceFactory(VideoToResourceConverter(), SubjectToResourceConverter(), videoService))
    }

    @Bean
    fun addVideoToCollection(): AddVideoToCollection {
        return AddVideoToCollection(collectionService, analyticsEventService)
    }

    @Bean
    fun removeVideoFromCollection(): RemoveVideoFromCollection {
        return RemoveVideoFromCollection(collectionService, analyticsEventService)
    }

    @Bean
    fun updateCollection(): UpdateCollection {
        return UpdateCollection(collectionService, analyticsEventService)
    }

    @Bean
    fun bookmarkCollection(): BookmarkCollection {
        return BookmarkCollection(collectionService, analyticsEventService)
    }

    @Bean
    fun unbookmarkCollection(): UnbookmarkCollection {
        return UnbookmarkCollection(collectionService, analyticsEventService)
    }

    @Bean
    fun deleteCollection(): DeleteCollection {
        return DeleteCollection(collectionService)
    }

    @Bean
    fun rebuildSearchIndex(): RebuildSearchIndex {
        return RebuildSearchIndex(videoAssetRepository, searchService)
    }

    @Bean
    fun buildLegacySearchIndex(): BuildLegacySearchIndex {
        return BuildLegacySearchIndex(videoAssetRepository, legacySearchService)
    }

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoService, topics)
    }

    @Bean
    fun analyseContentPartnerVideos(): AnalyseContentPartnerVideos {
        return AnalyseContentPartnerVideos(videoAssetRepository, analyseVideo())
    }

    @Bean
    fun refreshVideoDurations(): RefreshVideoDurations {
        return RefreshVideoDurations(videoAssetRepository, playbackRepository)
    }

    @Bean
    fun getSubjects(): GetSubjects {
        return GetSubjects(subjectRepository)
    }

    @Bean
    fun createSubject(): CreateSubject {
        return CreateSubject(subjectRepository)
    }

    private fun getVideoById() =
        GetVideoById(
            videoService,
            videoToResourceConverter()
        )

    private fun getVideosByQuery() =
        GetVideosByQuery(
            videoService,
            videoToResourceConverter(),
            analyticsEventService
        )

    private fun getAllVideosById(): GetAllVideosById {
        return GetAllVideosById(
            videoService,
            videoToResourceConverter()
        )
    }

    private fun videoToResourceConverter(): VideoToResourceConverter {
        return VideoToResourceConverter()
    }
}
