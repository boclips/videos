package com.boclips.videos.service.config.application

import com.boclips.events.config.Topics
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
import com.boclips.videos.service.application.video.GetVideoTranscript
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.application.video.RequestVideoPlaybackUpdate
import com.boclips.videos.service.application.video.UpdateAnalysedVideo
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.search.GetAllVideosById
import com.boclips.videos.service.application.video.search.GetVideoById
import com.boclips.videos.service.application.video.search.GetVideosByQuery
import com.boclips.videos.service.application.video.search.SearchQueryConverter
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.CreateVideoRequestToVideoConverter
import com.boclips.videos.service.presentation.video.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
    val videoService: VideoService,
    val videoRepository: VideoRepository,
    val searchService: SearchService,
    val playbackRepository: PlaybackRepository,
    val legacySearchService: LegacySearchService,
    val collectionRepository: CollectionRepository,
    val analyticsEventService: AnalyticsEventService,
    val videoAccessService: VideoAccessService,
    val topics: Topics,
    val subjectRepository: SubjectRepository
) {

    @Bean
    fun searchVideo(
        videosLinkBuilder: VideosLinkBuilder,
        searchQueryConverter: SearchQueryConverter,
        playbackToResourceConverter: PlaybackToResourceConverter
    ) = SearchVideo(
        getVideoById(videosLinkBuilder, playbackToResourceConverter),
        getAllVideosById(videosLinkBuilder, playbackToResourceConverter),
        getVideosByQuery(videosLinkBuilder, searchQueryConverter, playbackToResourceConverter),
        videoRepository
    )

    @Bean
    fun createVideo(
        searchVideo: SearchVideo,
        videoCounter: Counter,
        analyseVideo: AnalyseVideo
    ): CreateVideo {
        return CreateVideo(
            videoRepository,
            searchVideo,
            CreateVideoRequestToVideoConverter(),
            searchService,
            playbackRepository,
            videoCounter,
            legacySearchService,
            analyseVideo
        )
    }

    @Bean
    fun updateVideo(): UpdateVideo {
        return UpdateVideo(videoRepository)
    }

    @Bean
    fun updateAnalysedVideo(): UpdateAnalysedVideo {
        return UpdateAnalysedVideo(playbackRepository, videoRepository)
    }

    @Bean
    fun bulkUpdate(): BulkUpdateVideo {
        return BulkUpdateVideo(videoRepository, searchService, legacySearchService, videoAccessService)
    }

    @Bean
    fun deleteVideos(): DeleteVideos {
        return DeleteVideos(videoRepository, searchService, playbackRepository)
    }

    @Bean
    fun createCollection(addVideoToCollection: AddVideoToCollection): CreateCollection {
        return CreateCollection(collectionRepository, addVideoToCollection)
    }

    @Bean
    fun getCollection(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter
    ): GetCollection {
        return GetCollection(
            collectionRepository,
            CollectionResourceFactory(
                VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
                SubjectToResourceConverter(),
                videoService
            )
        )
    }

    @Bean
    fun getPublicCollections(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter
    ): GetCollections {
        return GetCollections(
            collectionRepository,
            CollectionResourceFactory(
                VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
                SubjectToResourceConverter(),
                videoService
            )
        )
    }

    @Bean
    fun addVideoToCollection(): AddVideoToCollection {
        return AddVideoToCollection(collectionRepository, analyticsEventService)
    }

    @Bean
    fun removeVideoFromCollection(): RemoveVideoFromCollection {
        return RemoveVideoFromCollection(collectionRepository, analyticsEventService)
    }

    @Bean
    fun updateCollection(): UpdateCollection {
        return UpdateCollection(collectionRepository, analyticsEventService)
    }

    @Bean
    fun bookmarkCollection(): BookmarkCollection {
        return BookmarkCollection(collectionRepository, analyticsEventService)
    }

    @Bean
    fun unbookmarkCollection(): UnbookmarkCollection {
        return UnbookmarkCollection(collectionRepository, analyticsEventService)
    }

    @Bean
    fun deleteCollection(): DeleteCollection {
        return DeleteCollection(collectionRepository)
    }

    @Bean
    fun rebuildSearchIndex(): RebuildSearchIndex {
        return RebuildSearchIndex(videoRepository, searchService)
    }

    @Bean
    fun buildLegacySearchIndex(): BuildLegacySearchIndex {
        return BuildLegacySearchIndex(videoRepository, legacySearchService)
    }

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoService, topics)
    }

    @Bean
    fun analyseContentPartnerVideos(): AnalyseContentPartnerVideos {
        return AnalyseContentPartnerVideos(videoRepository, analyseVideo())
    }

    @Bean
    fun refreshVideoDurations(): RequestVideoPlaybackUpdate {
        return RequestVideoPlaybackUpdate(videoRepository, playbackRepository, topics)
    }

    @Bean
    fun getSubjects(): GetSubjects {
        return GetSubjects(subjectRepository)
    }

    @Bean
    fun createSubject(): CreateSubject {
        return CreateSubject(subjectRepository)
    }

    @Bean
    fun getVideoTranscript(): GetVideoTranscript {
        return GetVideoTranscript(videoRepository)
    }

    @Bean
    fun stringToDurationConverter(): SearchQueryConverter {
        return SearchQueryConverter()
    }

    private fun getVideoById(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter
    ) =
        GetVideoById(
            videoService,
            videoToResourceConverter(videosLinkBuilder, playbackToResourceConverter)
        )

    private fun getVideosByQuery(
        videosLinkBuilder: VideosLinkBuilder,
        searchQueryConverter: SearchQueryConverter, playbackToResourceConverter: PlaybackToResourceConverter
    ) =
        GetVideosByQuery(
            videoService,
            videoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
            analyticsEventService,
            searchQueryConverter
        )

    private fun getAllVideosById(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter
    ): GetAllVideosById {
        return GetAllVideosById(
            videoService,
            videoToResourceConverter(videosLinkBuilder, playbackToResourceConverter)
        )
    }

    private fun videoToResourceConverter(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter
    ): VideoToResourceConverter {
        return VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter)
    }
}
