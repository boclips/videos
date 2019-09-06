package com.boclips.videos.service.config.application

import com.boclips.eventbus.EventBus
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CollectionUpdatesConverter
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.GetContractedCollections
import com.boclips.videos.service.application.collection.GetViewerCollections
import com.boclips.videos.service.application.collection.RebuildCollectionIndex
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.contentPartner.CreateContentPartner
import com.boclips.videos.service.application.contentPartner.GetContentPartner
import com.boclips.videos.service.application.contentPartner.GetContentPartners
import com.boclips.videos.service.application.contentPartner.UpdateContentPartner
import com.boclips.videos.service.application.disciplines.CreateDiscipline
import com.boclips.videos.service.application.disciplines.GetDiscipline
import com.boclips.videos.service.application.disciplines.GetDisciplines
import com.boclips.videos.service.application.disciplines.ReplaceDisciplineSubjects
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.DeleteSubject
import com.boclips.videos.service.application.subject.GetSubject
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.subject.UpdateSubject
import com.boclips.videos.service.application.tag.CreateTag
import com.boclips.videos.service.application.tag.DeleteTag
import com.boclips.videos.service.application.tag.GetTag
import com.boclips.videos.service.application.tag.GetTags
import com.boclips.videos.service.application.video.BroadcastVideos
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.RateVideo
import com.boclips.videos.service.application.video.RebuildLegacySearchIndex
import com.boclips.videos.service.application.video.RebuildVideoIndex
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.VideoAnalysisService
import com.boclips.videos.service.application.video.VideoPlaybackService
import com.boclips.videos.service.application.video.VideoSearchUpdater
import com.boclips.videos.service.application.video.VideoTranscriptService
import com.boclips.videos.service.application.video.search.GetAllVideosById
import com.boclips.videos.service.application.video.search.GetVideoById
import com.boclips.videos.service.application.video.search.GetVideosByQuery
import com.boclips.videos.service.application.video.search.SearchQueryConverter
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.service.presentation.attachments.AttachmentToResourceConverter
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
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
        videoAnalysisService: VideoAnalysisService,
        subjectClassificationService: SubjectClassificationService
    ): CreateVideo {
        return CreateVideo(
            videoService,
            videoRepository,
            subjectRepository,
            contentPartnerRepository,
            searchVideo,
            CreateVideoRequestToVideoConverter(),
            playbackRepository,
            videoCounter,
            videoAnalysisService,
            subjectClassificationService
        )
    }

    @Bean
    fun rateVideo(): RateVideo {
        return RateVideo(videoRepository)
    }

    @Bean
    fun updateVideo(): UpdateVideo {
        return UpdateVideo(videoRepository)
    }

    @Bean
    fun tagVideo(): TagVideo {
        return TagVideo(videoRepository, tagRepository)
    }

    @Bean
    fun updateVideoCaptions(): UpdateCaptions {
        return UpdateCaptions(videoRepository, playbackRepository)
    }

    @Bean
    fun updateVideoTranscripts(): VideoTranscriptService {
        return VideoTranscriptService(videoRepository)
    }

    @Bean
    fun bulkVideoUpdate(): BulkUpdateVideo {
        return BulkUpdateVideo(videoRepository)
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
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder
    ): GetCollection {
        return GetCollection(
            collectionRepository,
            CollectionResourceFactory(
                VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
                SubjectToResourceConverter(),
                AttachmentToResourceConverter(attachmentsLinkBuilder),
                videoService
            )
        )
    }

    @Bean
    fun getCollections(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder,
        getContractedCollections: GetContractedCollections,
        userServiceClient: UserServiceClient
    ): GetCollections {
        return GetCollections(
            collectionService,
            collectionRepository,
            CollectionResourceFactory(
                VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
                SubjectToResourceConverter(),
                AttachmentToResourceConverter(attachmentsLinkBuilder),
                videoService
            ),
            getContractedCollections,
            userServiceClient
        )
    }

    @Bean
    fun getContractedCollections(collectionRepository: CollectionRepository): GetContractedCollections {
        return GetContractedCollections(collectionRepository)
    }

    @Bean
    fun getViewerCollections(
        collectionRepository: CollectionRepository,
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder
    ): GetViewerCollections {
        return GetViewerCollections(
            collectionRepository, CollectionResourceFactory(
                VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
                SubjectToResourceConverter(),
                AttachmentToResourceConverter(attachmentsLinkBuilder),
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
    fun updateCollection(collectionUpdatesConverter: CollectionUpdatesConverter): UpdateCollection {
        return UpdateCollection(collectionSearchService, collectionRepository, eventService, collectionUpdatesConverter)
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
    fun buildLegacySearchIndex(): RebuildLegacySearchIndex {
        return RebuildLegacySearchIndex(videoRepository, legacyVideoSearchService)
    }

    @Bean
    fun videoAnalysisService(): VideoAnalysisService {
        return VideoAnalysisService(videoRepository, videoService, eventBus, playbackRepository)
    }

    @Bean
    fun videoPlaybackService(): VideoPlaybackService {
        return VideoPlaybackService(videoRepository, eventBus, playbackRepository)
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
    fun updateSubject(batchProcessingConfig: BatchProcessingConfig): UpdateSubject {
        return UpdateSubject(subjectRepository, videoRepository, collectionRepository, batchProcessingConfig)
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
    fun stringToDurationConverter(): SearchQueryConverter {
        return SearchQueryConverter()
    }

    @Bean
    fun createContentPartner(): CreateContentPartner {
        return CreateContentPartner(contentPartnerRepository)
    }

    @Bean
    fun updateContentPartner(): UpdateContentPartner {
        return UpdateContentPartner(contentPartnerRepository, videoService)
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
    fun broadcastVideos(): BroadcastVideos {
        return BroadcastVideos(videoRepository, eventBus)
    }

    @Bean
    fun collectionUpdatesConverter(): CollectionUpdatesConverter {
        return CollectionUpdatesConverter(subjectRepository)
    }

    @Bean
    fun bulkUpdateVideo(): BulkUpdateVideo {
        return BulkUpdateVideo(videoRepository)
    }

    @Bean
    fun videoUpdateService(): VideoSearchUpdater {
        return VideoSearchUpdater(videoRepository, videoSearchService, legacyVideoSearchService)
    }

    private fun getVideoById(videoToResourceConverter: VideoToResourceConverter): GetVideoById {
        return GetVideoById(
            videoService,
            videoToResourceConverter
        )
    }

    private fun getVideosByQuery(
        searchQueryConverter: SearchQueryConverter,
        videoToResourceConverter: VideoToResourceConverter
    ): GetVideosByQuery {
        return GetVideosByQuery(
            videoService,
            videoToResourceConverter,
            eventService,
            searchQueryConverter
        )
    }

    private fun getAllVideosById(videoToResourceConverter: VideoToResourceConverter): GetAllVideosById {
        return GetAllVideosById(
            videoService,
            videoToResourceConverter
        )
    }
}
