package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.application.legalrestriction.FindAllLegalRestrictions
import com.boclips.contentpartner.service.application.legalrestriction.FindLegalRestrictions
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
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
import com.boclips.videos.service.application.search.FindSuggestions
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
import com.boclips.videos.service.application.video.UpdateCaptionContent
import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.VideoAnalysisService
import com.boclips.videos.service.application.video.VideoCaptionService
import com.boclips.videos.service.application.video.VideoPlaybackService
import com.boclips.videos.service.application.video.VideoTranscriptService
import com.boclips.videos.service.application.video.indexing.RebuildLegacySearchIndex
import com.boclips.videos.service.application.video.indexing.RebuildVideoIndex
import com.boclips.videos.service.application.video.indexing.VideoIndexUpdater
import com.boclips.videos.service.application.video.search.GetVideoById
import com.boclips.videos.service.application.video.search.GetVideosByQuery
import com.boclips.videos.service.application.video.search.SearchQueryConverter
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.DisciplineRepository
import com.boclips.videos.service.domain.service.TagRepository
import com.boclips.videos.service.domain.service.collection.CollectionCreationService
import com.boclips.videos.service.domain.service.collection.CollectionDeletionService
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.CaptionValidator
import com.boclips.videos.service.domain.service.video.VideoCreationService
import com.boclips.videos.service.domain.service.video.VideoDeletionService
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoRetrievalService
import com.boclips.videos.service.infrastructure.captions.ExoWebVTTValidator
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
    val videoRetrievalService: VideoRetrievalService,
    val videoRepository: VideoRepository,
    val videoIndex: VideoIndex,
    val collectionIndex: CollectionIndex,
    val playbackRepository: PlaybackRepository,
    val legacyVideoSearchService: LegacyVideoSearchService,
    val collectionRetrievalService: CollectionRetrievalService,
    val collectionCreationService: CollectionCreationService,
    val collectionRepository: CollectionRepository,
    val eventService: EventService,
    val eventBus: EventBus,
    val ageRangeRepository: AgeRangeRepository,
    val subjectRepository: SubjectRepository,
    val tagRepository: TagRepository,
    val disciplineRepository: DisciplineRepository,
    val contentPartnerService: ContentPartnerService,
    val userService: UserService,
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val accessRuleService: AccessRuleService,
    val videoCreationService: VideoCreationService
) {
    @Bean
    fun searchVideo(
        getVideoById: GetVideoById,
        getVideosByQuery: GetVideosByQuery,
        searchQueryConverter: SearchQueryConverter
    ) = SearchVideo(
        getVideoById,
        getVideosByQuery,
        videoRepository
    )

    @Bean
    fun suggestSearchQuery(contentPartnerRepository: ContentPartnerRepository): FindSuggestions =
        FindSuggestions(contentPartnerRepository)

    @Bean
    fun createVideo(
        videoCounter: Counter,
        videoAnalysisService: VideoAnalysisService,
        subjectClassificationService: SubjectClassificationService
    ): CreateVideo {
        return CreateVideo(
            videoCreationService,
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
        return UpdateVideo(videoRepository, subjectRepository, tagRepository)
    }

    @Bean
    fun updateCaptionContent(captionService: CaptionService): UpdateCaptionContent {
        return UpdateCaptionContent(captionService)
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
    fun videoCaptionService(kalturaClient: KalturaClient): VideoCaptionService {
        return VideoCaptionService(kalturaClient)
    }

    @Bean
    fun captionValidator(): CaptionValidator {
        return ExoWebVTTValidator()
    }

    @Bean
    fun captionService(captionValidator: CaptionValidator): CaptionService {
        return CaptionService(videoRepository, playbackRepository, captionValidator)
    }

    @Bean
    fun deleteVideos(videoDeletionService: VideoDeletionService): DeleteVideo {
        return DeleteVideo(videoDeletionService)
    }

    @Bean
    fun createCollection(): CreateCollection {
        return CreateCollection(collectionCreationService, collectionIndex)
    }

    @Bean
    fun getCollection(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder
    ): GetCollection {
        return GetCollection(collectionRetrievalService)
    }

    @Bean
    fun getCollections(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder,
        collectionFilterAssembler: CollectionSearchQueryAssembler
    ): GetCollections {
        return GetCollections(
            collectionRetrievalService,
            collectionFilterAssembler
        )
    }

    @Bean
    fun assembleCollectionFilter() = CollectionSearchQueryAssembler()

    @Bean
    fun addVideoToCollection(): AddVideoToCollection {
        return AddVideoToCollection(collectionRepository, collectionRetrievalService)
    }

    @Bean
    fun removeVideoFromCollection(): RemoveVideoFromCollection {
        return RemoveVideoFromCollection(collectionRepository, collectionRetrievalService)
    }

    @Bean
    fun updateCollection(collectionUpdatesConverter: CollectionUpdatesConverter): UpdateCollection {
        return UpdateCollection(
            collectionIndex,
            collectionRepository,
            collectionUpdatesConverter,
            collectionRetrievalService
        )
    }

    @Bean
    fun bookmarkCollection(): BookmarkCollection {
        return BookmarkCollection(collectionRepository, collectionIndex, collectionRetrievalService)
    }

    @Bean
    fun unbookmarkCollection(): UnbookmarkCollection {
        return UnbookmarkCollection(collectionRepository, collectionIndex, collectionRetrievalService)
    }

    @Bean
    fun deleteCollection(collectionDeletionService: CollectionDeletionService): DeleteCollection {
        return DeleteCollection(collectionDeletionService)
    }

    @Bean
    fun rebuildSearchIndex(): RebuildVideoIndex {
        return RebuildVideoIndex(
            videoRepository,
            videoIndex
        )
    }

    @Bean
    fun rebuildCollectionSearchIndex(): RebuildCollectionIndex {
        return RebuildCollectionIndex(collectionRepository, collectionIndex)
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
        return VideoAnalysisService(videoRepository, eventBus, playbackRepository)
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
        return DeleteSubject(subjectRepository, collectionRepository, collectionIndex)
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
            videoIndex,
            legacyVideoSearchService
        )
    }

    @Bean
    fun contentPartnerUpdated(): ContentPartnerUpdated {
        return ContentPartnerUpdated(videoRepository)
    }

    @Bean
    fun createLegalRestrictions(): CreateLegalRestrictions {
        return CreateLegalRestrictions(
            legalRestrictionsRepository
        )
    }

    @Bean
    fun findLegalRestrictions(): FindLegalRestrictions {
        return FindLegalRestrictions(
            legalRestrictionsRepository
        )
    }

    @Bean
    fun findAllLegalRestrictions(): FindAllLegalRestrictions {
        return FindAllLegalRestrictions(
            legalRestrictionsRepository
        )
    }

    @Bean
    fun getVideoById(): GetVideoById {
        return GetVideoById(videoRetrievalService)
    }

    @Bean
    fun getVideosByQuery(searchQueryConverter: SearchQueryConverter): GetVideosByQuery {
        return GetVideosByQuery(
            videoRetrievalService,
            eventService,
            userService,
            searchQueryConverter
        )
    }
}
