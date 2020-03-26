package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.application.CreateLegalRestrictions
import com.boclips.contentpartner.service.application.FindAllLegalRestrictions
import com.boclips.contentpartner.service.application.FindLegalRestrictions
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.application.ContentPartnerUpdated
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CollectionSearchQueryAssembler
import com.boclips.videos.service.application.collection.CollectionUpdatesConverter
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.RebuildCollectionIndex
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.disciplines.CreateDiscipline
import com.boclips.videos.service.application.disciplines.GetDiscipline
import com.boclips.videos.service.application.disciplines.GetDisciplines
import com.boclips.videos.service.application.disciplines.ReplaceDisciplineSubjects
import com.boclips.videos.service.application.disciplines.UpdateDiscipline
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
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideo
import com.boclips.videos.service.application.video.RateVideo
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.VideoAnalysisService
import com.boclips.videos.service.application.video.VideoPlaybackService
import com.boclips.videos.service.application.video.VideoTranscriptService
import com.boclips.videos.service.application.video.indexing.RebuildLegacySearchIndex
import com.boclips.videos.service.application.video.indexing.RebuildVideoIndex
import com.boclips.videos.service.application.video.indexing.VideoIndexUpdater
import com.boclips.videos.service.application.video.search.GetAllVideosById
import com.boclips.videos.service.application.video.search.GetVideoById
import com.boclips.videos.service.application.video.search.GetVideosByQuery
import com.boclips.videos.service.application.video.search.SearchQueryConverter
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.collection.CollectionCreationService
import com.boclips.videos.service.domain.service.collection.CollectionReadService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.converters.CreateVideoRequestToVideoConverter
import com.boclips.videos.service.presentation.converters.DisciplineConverter
import com.boclips.videos.service.presentation.converters.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
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
    val collectionReadService: CollectionReadService,
    val collectionCreationService: CollectionCreationService,
    val collectionRepository: CollectionRepository,
    val eventService: EventService,
    val eventBus: EventBus,
    val subjectRepository: SubjectRepository,
    val tagRepository: TagRepository,
    val disciplineRepository: DisciplineRepository,
    val contentPartnerService: ContentPartnerService,
    val userService: UserService,
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val accessRuleService: AccessRuleService
) {
    @Bean
    fun searchVideo(
        getVideoById: GetVideoById,
        getAllVideosById: GetAllVideosById,
        getVideosByQuery: GetVideosByQuery,
        searchQueryConverter: SearchQueryConverter
    ) = SearchVideo(
        getVideoById,
        getVideosByQuery,
        getAllVideosById,
        videoRepository
    )

    @Bean
    fun createVideo(
        videoCounter: Counter,
        videoAnalysisService: VideoAnalysisService,
        subjectClassificationService: SubjectClassificationService
    ): CreateVideo {
        return CreateVideo(
            videoService,
            subjectRepository,
            contentPartnerService,
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
        return UpdateVideo(videoRepository, subjectRepository)
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
    fun deleteVideos(): DeleteVideo {
        return DeleteVideo(videoRepository, collectionRepository, videoSearchService, playbackRepository)
    }

    @Bean
    fun createCollection(): CreateCollection {
        return CreateCollection(collectionCreationService, collectionSearchService)
    }

    @Bean
    fun getCollection(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder
    ): GetCollection {
        return GetCollection(collectionReadService)
    }

    @Bean
    fun getCollections(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder,
        collectionFilterAssembler: CollectionSearchQueryAssembler
    ): GetCollections {
        return GetCollections(
            collectionReadService,
            collectionFilterAssembler
        )
    }

    @Bean
    fun assembleCollectionFilter() = CollectionSearchQueryAssembler()

    @Bean
    fun addVideoToCollection(): AddVideoToCollection {
        return AddVideoToCollection(collectionRepository, collectionReadService)
    }

    @Bean
    fun removeVideoFromCollection(): RemoveVideoFromCollection {
        return RemoveVideoFromCollection(collectionRepository, collectionReadService)
    }

    @Bean
    fun updateCollection(collectionUpdatesConverter: CollectionUpdatesConverter): UpdateCollection {
        return UpdateCollection(
            collectionSearchService,
            collectionRepository,
            collectionUpdatesConverter,
            collectionReadService
        )
    }

    @Bean
    fun bookmarkCollection(): BookmarkCollection {
        return BookmarkCollection(collectionRepository, collectionSearchService, collectionReadService)
    }

    @Bean
    fun unbookmarkCollection(): UnbookmarkCollection {
        return UnbookmarkCollection(collectionRepository, collectionSearchService, collectionReadService)
    }

    @Bean
    fun deleteCollection(): DeleteCollection {
        return DeleteCollection(collectionRepository, collectionSearchService, collectionReadService)
    }

    @Bean
    fun rebuildSearchIndex(): RebuildVideoIndex {
        return RebuildVideoIndex(
            videoRepository,
            videoSearchService
        )
    }

    @Bean
    fun rebuildCollectionSearchIndex(): RebuildCollectionIndex {
        return RebuildCollectionIndex(collectionRepository, collectionSearchService)
    }

    @Bean
    fun buildLegacySearchIndex(): RebuildLegacySearchIndex {
        return RebuildLegacySearchIndex(
            videoRepository,
            contentPartnerService,
            legacyVideoSearchService
        )
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
    fun updateSubject(): UpdateSubject {
        return UpdateSubject(subjectRepository, videoRepository, collectionRepository)
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
    fun disciplineConverter(disciplinesLinkBuilder: DisciplinesLinkBuilder): DisciplineConverter {
        return DisciplineConverter(disciplinesLinkBuilder = disciplinesLinkBuilder)
    }

    @Bean
    fun getDiscipline(disciplineConverter: DisciplineConverter): GetDiscipline {
        return GetDiscipline(disciplineRepository = disciplineRepository, disciplineConverter = disciplineConverter)
    }

    @Bean
    fun getDisciplines(disciplineConverter: DisciplineConverter): GetDisciplines {
        return GetDisciplines(disciplineRepository, disciplineConverter)
    }

    @Bean
    fun createDiscipline(disciplineConverter: DisciplineConverter): CreateDiscipline {
        return CreateDiscipline(disciplineRepository)
    }

    @Bean
    fun replaceDisciplineSubjects(disciplineConverter: DisciplineConverter): ReplaceDisciplineSubjects {
        return ReplaceDisciplineSubjects(disciplineRepository, subjectRepository)
    }

    @Bean
    fun updateDiscipline(): UpdateDiscipline {
        return UpdateDiscipline(disciplineRepository)
    }

    @Bean
    fun stringToDurationConverter(): SearchQueryConverter {
        return SearchQueryConverter()
    }

    @Bean
    fun broadcastVideos(): BroadcastVideos {
        return BroadcastVideos(videoRepository, eventBus)
    }

    @Bean
    fun videoUpdateService(): VideoIndexUpdater {
        return VideoIndexUpdater(
            videoRepository,
            contentPartnerService,
            videoSearchService,
            legacyVideoSearchService
        )
    }

    @Bean
    fun contentPartnerUpdated(): ContentPartnerUpdated {
        return ContentPartnerUpdated(videoRepository)
    }

    @Bean
    fun createLegalRestrictions(): CreateLegalRestrictions {
        return CreateLegalRestrictions(legalRestrictionsRepository)
    }

    @Bean
    fun findLegalRestrictions(): FindLegalRestrictions {
        return FindLegalRestrictions(legalRestrictionsRepository)
    }

    @Bean
    fun findAllLegalRestrictions(): FindAllLegalRestrictions {
        return FindAllLegalRestrictions(legalRestrictionsRepository)
    }

    @Bean
    fun getVideoById(): GetVideoById {
        return GetVideoById(videoService)
    }

    @Bean
    fun getVideosByQuery(searchQueryConverter: SearchQueryConverter): GetVideosByQuery {
        return GetVideosByQuery(
            videoService,
            eventService,
            userService,
            searchQueryConverter
        )
    }

    @Bean
    fun getAllVideosById(): GetAllVideosById {
        return GetAllVideosById(videoService)
    }
}
