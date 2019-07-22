package com.boclips.videos.service.config.application


import com.boclips.eventbus.EventBus
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.RebuildCollectionIndex
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.contentPartner.CreateContentPartner
import com.boclips.videos.service.application.contentPartner.GetContentPartner
import com.boclips.videos.service.application.contentPartner.GetContentPartners
import com.boclips.videos.service.application.contentPartner.RequestBulkVideoSearchUpdateByContentPartner
import com.boclips.videos.service.application.contentPartner.UpdateContentPartner
import com.boclips.videos.service.application.disciplines.CreateDiscipline
import com.boclips.videos.service.application.disciplines.GetDiscipline
import com.boclips.videos.service.application.disciplines.GetDisciplines
import com.boclips.videos.service.application.disciplines.ReplaceDisciplineSubjects
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.DeleteSubject
import com.boclips.videos.service.application.subject.GetSubject
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.application.tag.CreateTag
import com.boclips.videos.service.application.tag.DeleteTag
import com.boclips.videos.service.application.tag.GetTag
import com.boclips.videos.service.application.tag.GetTags
import com.boclips.videos.service.application.video.AnalyseContentPartnerVideos
import com.boclips.videos.service.application.video.AnalyseVideo
import com.boclips.videos.service.application.video.BuildLegacySearchIndex
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.BulkVideoSearchUpdate
import com.boclips.videos.service.application.video.ClassifyContentPartnerVideos
import com.boclips.videos.service.application.video.ClassifyVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.DispatchVideoUpdatedEvents
import com.boclips.videos.service.application.video.GetVideoTranscript
import com.boclips.videos.service.application.video.RateVideo
import com.boclips.videos.service.application.video.RebuildVideoIndex
import com.boclips.videos.service.application.video.RequestPlaybackUpdate
import com.boclips.videos.service.application.video.UpdateAnalysedVideo
import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.application.video.UpdatePlayback
import com.boclips.videos.service.application.video.UpdateTranscripts
import com.boclips.videos.service.application.video.UpdateVideoSubjects
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForDownload
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForStream
import com.boclips.videos.service.application.video.search.GetAllVideosById
import com.boclips.videos.service.application.video.search.GetVideoById
import com.boclips.videos.service.application.video.search.GetVideosByQuery
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.application.video.search.SearchQueryConverter
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.config.properties.PubSubVideoSearchabilityUpdateProperties
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.hateoas.ContentPartnersLinkBuilder
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
    val videoSearchService: VideoSearchService,
    val collectionSearchService: CollectionSearchService,
    val playbackRepository: PlaybackRepository,
    val legacyVideoSearchService: LegacyVideoSearchService,
    val collectionService: CollectionService,
    val collectionRepository: CollectionRepository,
    val eventService: EventService,
    val eventBus: EventBus,
    val subjectRepository: SubjectRepository,
    val tagRepository: TagRepository,
    val disciplineRepository: DisciplineRepository,
    val contentPartnerRepository: ContentPartnerRepository,
    val contentPartnersLinkBuilder: ContentPartnersLinkBuilder
) {

    @Bean
    fun searchVideo(
        videosLinkBuilder: VideosLinkBuilder,
        searchQueryConverter: SearchQueryConverter,
        playbackToResourceConverter: PlaybackToResourceConverter,
        videoToResourceConverter: VideoToResourceConverter
    ) = SearchVideo(
        getVideoById(videoToResourceConverter),
        getAllVideosById(videoToResourceConverter),
        getVideosByQuery(searchQueryConverter, videoToResourceConverter),
        videoRepository
    )

    @Bean
    fun createVideo(
        searchVideo: SearchVideo,
        videoCounter: Counter,
        analyseVideo: AnalyseVideo
    ): CreateVideo {
        return CreateVideo(
            videoService,
            videoRepository,
            subjectRepository,
            contentPartnerRepository,
            searchVideo,
            CreateVideoRequestToVideoConverter(),
            playbackRepository,
            analyseVideo,
            eventBus
        )
    }

    @Bean
    fun rateVideo(): RateVideo {
        return RateVideo(videoRepository)
    }

    @Bean
    fun updateAnalysedVideo(): UpdateAnalysedVideo {
        return UpdateAnalysedVideo(playbackRepository, videoRepository, videoSearchService)
    }

    @Bean
    fun updateVideoCaptions(): UpdateCaptions {
        return UpdateCaptions(videoRepository, playbackRepository)
    }

    @Bean
    fun updateVideoTranscripts(): UpdateTranscripts {
        return UpdateTranscripts(videoRepository)
    }

    @Bean
    fun updateVideoSubjects(): UpdateVideoSubjects {
        return UpdateVideoSubjects(videoRepository, subjectRepository, videoSearchService)
    }

    @Bean
    fun excludeVideosFromSearchForStream(): ExcludeVideosFromSearchForStream {
        return ExcludeVideosFromSearchForStream(eventBus = eventBus)
    }

    @Bean
    fun excludeVideosFromSearchForDownload(): ExcludeVideosFromSearchForDownload {
        return ExcludeVideosFromSearchForDownload(eventBus = eventBus)
    }

    @Bean
    fun bulkUpdate(
        includeVideosInSearchForStream: IncludeVideosInSearchForStream,
        excludeVideosFromSearchForStream: ExcludeVideosFromSearchForStream,
        includeVideosInSearchForDownload: IncludeVideosInSearchForDownload,
        excludeVideosFromSearchForDownload: ExcludeVideosFromSearchForDownload
    ): BulkUpdateVideo {
        return BulkUpdateVideo(
            videoRepository,
            includeVideosInSearchForStream,
            excludeVideosFromSearchForStream,
            excludeVideosFromSearchForDownload,
            includeVideosInSearchForDownload
        )
    }

    @Bean
    fun deleteVideos(): DeleteVideo {
        return DeleteVideo(videoRepository, collectionRepository, videoSearchService, playbackRepository)
    }

    @Bean
    fun createCollection(addVideoToCollection: AddVideoToCollection): CreateCollection {
        return CreateCollection(collectionRepository, addVideoToCollection, collectionSearchService)
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
            collectionService,
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
        return AddVideoToCollection(collectionRepository, eventService)
    }

    @Bean
    fun removeVideoFromCollection(): RemoveVideoFromCollection {
        return RemoveVideoFromCollection(collectionRepository, eventService)
    }

    @Bean
    fun updateCollection(): UpdateCollection {
        return UpdateCollection(collectionSearchService, collectionRepository, eventService)
    }

    @Bean
    fun bookmarkCollection(): BookmarkCollection {
        return BookmarkCollection(collectionRepository, eventService)
    }

    @Bean
    fun unbookmarkCollection(): UnbookmarkCollection {
        return UnbookmarkCollection(collectionRepository, eventService)
    }

    @Bean
    fun deleteCollection(): DeleteCollection {
        return DeleteCollection(collectionRepository, collectionSearchService)
    }

    @Bean
    fun rebuildSearchIndex(): RebuildVideoIndex {
        return RebuildVideoIndex(videoRepository, videoSearchService)
    }

    @Bean
    fun rebuildCollectionSearchIndex(): RebuildCollectionIndex {
        return RebuildCollectionIndex(collectionRepository, collectionSearchService)
    }

    @Bean
    fun buildLegacySearchIndex(): BuildLegacySearchIndex {
        return BuildLegacySearchIndex(videoRepository, legacyVideoSearchService)
    }

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoService, eventBus)
    }

    @Bean
    fun analyseContentPartnerVideos(): AnalyseContentPartnerVideos {
        return AnalyseContentPartnerVideos(videoRepository, analyseVideo())
    }

    @Bean
    fun classifyContentPartnerVideos(classifyVideo: ClassifyVideo): ClassifyContentPartnerVideos {
        return ClassifyContentPartnerVideos(videoRepository, classifyVideo)
    }

    @Bean
    fun refreshVideoDurations(): RequestPlaybackUpdate {
        return RequestPlaybackUpdate(videoRepository, eventBus)
    }

    @Bean
    fun updatePlayback(): UpdatePlayback {
        return UpdatePlayback(videoRepository, playbackRepository)
    }

    @Bean
    fun getSubject(): GetSubject {
        return GetSubject(subjectRepository)
    }

    @Bean
    fun deleteSubject(): DeleteSubject {
        return DeleteSubject(subjectRepository, collectionRepository, collectionSearchService)
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
    fun getTag(): GetTag {
        return GetTag(tagRepository)
    }

    @Bean
    fun deleteTag(): DeleteTag {
        return DeleteTag(tagRepository)
    }

    @Bean
    fun getTags(): GetTags {
        return GetTags(tagRepository)
    }

    @Bean
    fun createTag(): CreateTag {
        return CreateTag(tagRepository)
    }

    @Bean
    fun getDiscipline(): GetDiscipline {
        return GetDiscipline(disciplineRepository)
    }

    @Bean
    fun getDisciplines(): GetDisciplines {
        return GetDisciplines(disciplineRepository)
    }

    @Bean
    fun createDiscipline(): CreateDiscipline {
        return CreateDiscipline(disciplineRepository)
    }

    @Bean
    fun replaceDisciplineSubjects(): ReplaceDisciplineSubjects {
        return ReplaceDisciplineSubjects(disciplineRepository, subjectRepository)
    }

    @Bean
    fun getVideoTranscript(): GetVideoTranscript {
        return GetVideoTranscript(videoRepository)
    }

    @Bean
    fun stringToDurationConverter(): SearchQueryConverter {
        return SearchQueryConverter()
    }

    @Bean
    fun createContentPartner(): CreateContentPartner {
        return CreateContentPartner(contentPartnerRepository)
    }

    @Bean
    fun updateContentPartner(
        pubSubVideoSearchabilityUpdateProperties: PubSubVideoSearchabilityUpdateProperties,
        includeVideosInSearchForStream: IncludeVideosInSearchForStream,
        requestSearchUpdateByContentPartner: RequestBulkVideoSearchUpdateByContentPartner
    ): UpdateContentPartner {
        return UpdateContentPartner(
            contentPartnerRepository,
            videoRepository,
            requestSearchUpdateByContentPartner
        )
    }

    @Bean
    fun ageRangeToResourceConverter(): AgeRangeToResourceConverter {
        return AgeRangeToResourceConverter()
    }

    @Bean
    fun getContentPartner(): GetContentPartner {
        return GetContentPartner(contentPartnerRepository)
    }

    @Bean
    fun getContentPartners(): GetContentPartners {
        return GetContentPartners(contentPartnerRepository, contentPartnersLinkBuilder)
    }

    @Bean
    fun requestSearchUpdateByContentPartner(
        pubSubVideoSearchabilityUpdateProperties: PubSubVideoSearchabilityUpdateProperties,
        includeVideosInSearchForStream: IncludeVideosInSearchForStream,
        excludeVideosInSearchForStream: ExcludeVideosFromSearchForStream,
        includeVideosInSearchForDownload: IncludeVideosInSearchForDownload,
        excludeVideosInSearchForDownload: ExcludeVideosFromSearchForDownload
    ): RequestBulkVideoSearchUpdateByContentPartner {
        return RequestBulkVideoSearchUpdateByContentPartner(
            contentPartnerRepository,
            videoRepository,
            pubSubVideoSearchabilityUpdateProperties.batchSize,
            includeVideosInSearchForStream,
            excludeVideosInSearchForStream,
            includeVideosInSearchForDownload,
            excludeVideosInSearchForDownload
        )
    }

    @Bean
    fun getBulkVideoSearchUpdate(): BulkVideoSearchUpdate {
        return BulkVideoSearchUpdate(
            contentPartnerRepository,
            videoRepository,
            videoSearchService,
            legacyVideoSearchService
        )
    }

    @Bean
    fun dispatchVideoUpdatedEvents(): DispatchVideoUpdatedEvents {
        return DispatchVideoUpdatedEvents(videoRepository, eventBus)
    }

    private fun getVideoById(
        videoToResourceConverter: VideoToResourceConverter
    ) =
        GetVideoById(
            videoService,
            videoToResourceConverter
        )

    private fun getVideosByQuery(
        searchQueryConverter: SearchQueryConverter,
        videoToResourceConverter: VideoToResourceConverter
    ) =
        GetVideosByQuery(
            videoService,
            videoToResourceConverter,
            eventService,
            searchQueryConverter
        )

    private fun getAllVideosById(
        videoToResourceConverter: VideoToResourceConverter
    ): GetAllVideosById {
        return GetAllVideosById(
            videoService,
            videoToResourceConverter
        )
    }
}
