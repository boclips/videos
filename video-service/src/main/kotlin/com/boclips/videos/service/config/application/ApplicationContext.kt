package com.boclips.videos.service.config.application

import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.application.legalrestriction.FindAllLegalRestrictions
import com.boclips.contentpartner.service.application.legalrestriction.FindLegalRestrictions
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.eventbus.EventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.application.ChannelUpdated
import com.boclips.videos.service.application.attachment.GetAttachmentTypes
import com.boclips.videos.service.application.channels.RebuildChannelIndex
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.BroadcastCollections
import com.boclips.videos.service.application.collection.CollectionSearchQueryAssembler
import com.boclips.videos.service.application.collection.CollectionUpdatesConverter
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.CreateDefaultCollection
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetCollections
import com.boclips.videos.service.application.collection.GetCollectionsOfUser
import com.boclips.videos.service.application.collection.RebuildCollectionIndex
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.collection.UnbookmarkCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.common.QueryConverter
import com.boclips.videos.service.application.contentwarning.CreateContentWarning
import com.boclips.videos.service.application.contentwarning.GetAllContentWarnings
import com.boclips.videos.service.application.contentwarning.GetContentWarning
import com.boclips.videos.service.application.disciplines.CreateDiscipline
import com.boclips.videos.service.application.disciplines.GetDiscipline
import com.boclips.videos.service.application.disciplines.GetDisciplines
import com.boclips.videos.service.application.disciplines.ReplaceDisciplineSubjects
import com.boclips.videos.service.application.disciplines.UpdateDiscipline
import com.boclips.videos.service.application.search.FindSuggestions
import com.boclips.videos.service.application.search.FindSuggestionsByQuery
import com.boclips.videos.service.application.search.RebuildSubjectIndex
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
import com.boclips.videos.service.application.video.*
import com.boclips.videos.service.application.video.indexing.RebuildLegacySearchIndex
import com.boclips.videos.service.application.video.indexing.RebuildVideoIndex
import com.boclips.videos.service.application.video.indexing.VideoIndexUpdater
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
import com.boclips.videos.service.domain.service.subject.SubjectService
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import com.boclips.videos.service.domain.service.suggestions.SubjectIndex
import com.boclips.videos.service.domain.service.suggestions.SuggestionsRetrievalService
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.ContentPackageService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.CaptionValidator
import com.boclips.videos.service.domain.service.video.VideoCreationService
import com.boclips.videos.service.domain.service.video.VideoDeletionService
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.application.video.VideoRetrievalService
import com.boclips.videos.service.application.video.search.*
import com.boclips.videos.service.domain.model.video.PriceComputingService
import com.boclips.videos.service.domain.service.*
import com.boclips.videos.service.domain.service.video.plackback.PlaybackUpdateService
import com.boclips.videos.service.infrastructure.captions.ExoWebVTTValidator
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.presentation.converters.CreateVideoRequestToVideoConverter
import com.boclips.videos.service.presentation.converters.DisciplineConverter
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
    val suggestionsRetrievalService: SuggestionsRetrievalService,
    val channelIndex: ChannelIndex,
    val videoRepository: VideoRepository,
    val videoIndex: VideoIndex,
    val subjectIndex: SubjectIndex,
    val channelRepository: ChannelRepository,
    val channelsIndex: ChannelIndex,
    val collectionIndex: CollectionIndex,
    val playbackRepository: PlaybackRepository,
    val legacyVideoSearchService: LegacyVideoSearchService,
    val collectionRepository: CollectionRepository,
    val eventService: EventService,
    val eventBus: EventBus,
    val ageRangeRepository: AgeRangeRepository,
    val subjectRepository: SubjectRepository,
    val tagRepository: TagRepository,
    val disciplineRepository: DisciplineRepository,
    val videoChannelService: VideoChannelService,
    val userService: UserService,
    val legalRestrictionsRepository: LegalRestrictionsRepository,
    val accessRuleService: AccessRuleService,
    val videoCreationService: VideoCreationService,
    val subjectService: SubjectService,
    val contentWarningRepository: ContentWarningRepository,
    val contentPackageService: ContentPackageService,
    val organisationService: OrganisationService,
    val priceComputingService: PriceComputingService
) {
    @Bean
    fun searchVideo(
        getVideoById: GetVideoById,
        getVideosByQuery: GetVideosByQuery,
        queryConverter: QueryConverter,
        playbackUpdateService: PlaybackUpdateService,
        getVideoPrice: GetVideoPrice
    ) = SearchVideo(
        getVideoById,
        getVideosByQuery,
        videoRepository,
        playbackUpdateService,
        userService,
        priceComputingService,
        getVideoPrice
    )

    @Bean
    fun findNewSuggestions(findSuggestionsByQuery: FindSuggestionsByQuery) = FindSuggestions(findSuggestionsByQuery)

    @Bean
    fun findNewSuggestionsByQuery(): FindSuggestionsByQuery = FindSuggestionsByQuery(suggestionsRetrievalService)

    @Bean
    fun createVideo(
        videoCounter: Counter,
        videoAnalysisService: VideoAnalysisService,
        subjectClassificationService: SubjectClassificationService
    ): CreateVideo {
        return CreateVideo(
            videoCreationService,
            subjectRepository,
            videoChannelService,
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
    fun setVideoThumbnail(): SetVideoThumbnail {
        return SetVideoThumbnail(videoRepository)
    }

    @Bean
    fun uploadThumbnailImageToVideo(kalturaClient: KalturaClient, setVideoThumbnail: SetVideoThumbnail): UploadThumbnailImageToVideo {
        return UploadThumbnailImageToVideo(kalturaClient, setVideoThumbnail)
    }

    @Bean
    fun deleteVideoThumbnail(): DeleteVideoThumbnail {
        return DeleteVideoThumbnail(videoRepository)
    }

    @Bean
    fun updateVideo(): UpdateVideo {
        return UpdateVideo(videoRepository, subjectRepository, tagRepository, contentWarningRepository)
    }

    @Bean
    fun getVideosByContentPackage(videoRetrievalService: VideoRetrievalService): GetVideosByContentPackage =
        GetVideosByContentPackage(videoRetrievalService, contentPackageService)

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
        return VideoCaptionService(kalturaClient, captionService())
    }

    @Bean
    fun captionValidator(): CaptionValidator {
        return ExoWebVTTValidator()
    }

    @Bean
    fun captionService(): CaptionService {
        return CaptionService(videoRepository, playbackRepository, captionValidator())
    }

    @Bean
    fun deleteVideos(videoDeletionService: VideoDeletionService): DeleteVideo {
        return DeleteVideo(videoDeletionService)
    }

    @Bean
    fun createCollection(collectionCreationService: CollectionCreationService): CreateCollection {
        return CreateCollection(collectionCreationService)
    }

    @Bean
    fun createDefaultCollection(collectionCreationService: CollectionCreationService): CreateDefaultCollection {
        return CreateDefaultCollection(collectionCreationService)
    }

    @Bean
    fun getCollection(
        collectionAccessService: CollectionAccessService,
        collectionRetrievalService: CollectionRetrievalService
    ): GetCollection {
        return GetCollection(collectionRetrievalService, userService)
    }

    @Bean
    fun getCollections(
        collectionFilterAssembler: CollectionSearchQueryAssembler,
        collectionRetrievalService: CollectionRetrievalService
    ): GetCollections {
        return GetCollections(
            collectionRetrievalService,
            collectionFilterAssembler
        )
    }

    @Bean
    fun getCollectionsOfUser(
        collectionFilterAssembler: CollectionSearchQueryAssembler,
        collectionRetrievalService: CollectionRetrievalService
    ): GetCollectionsOfUser {
        return GetCollectionsOfUser(collectionRetrievalService, collectionFilterAssembler)
    }

    @Bean
    fun assembleCollectionFilter() = CollectionSearchQueryAssembler()

    @Bean
    fun addVideoToCollection(collectionUpdateService: CollectionUpdateService): AddVideoToCollection {
        return AddVideoToCollection(collectionUpdateService)
    }

    @Bean
    fun removeVideoFromCollection(collectionUpdateService: CollectionUpdateService): RemoveVideoFromCollection {
        return RemoveVideoFromCollection(collectionUpdateService)
    }

    @Bean
    fun updateCollection(
        collectionUpdatesConverter: CollectionUpdatesConverter,
        collectionUpdateService: CollectionUpdateService
    ): UpdateCollection {
        return UpdateCollection(
            collectionUpdatesConverter,
            collectionUpdateService
        )
    }

    @Bean
    fun bookmarkCollection(collectionBookmarkService: CollectionBookmarkService): BookmarkCollection {
        return BookmarkCollection(collectionBookmarkService)
    }

    @Bean
    fun unbookmarkCollection(collectionBookmarkService: CollectionBookmarkService): UnbookmarkCollection {
        return UnbookmarkCollection(collectionBookmarkService)
    }

    @Bean
    fun deleteCollection(collectionDeletionService: CollectionDeletionService): DeleteCollection {
        return DeleteCollection(collectionDeletionService)
    }

    @Bean
    fun rebuildSearchIndex(): RebuildVideoIndex {
        return RebuildVideoIndex(
            videoRepository,
            videoIndex,
            organisationService,
            priceComputingService
        )
    }

    @Bean
    fun rebuildSubjectIndex(): RebuildSubjectIndex {
        return RebuildSubjectIndex(
            subjectRepository,
            subjectIndex
        )
    }

    @Bean
    fun rebuildChannelIndex(): RebuildChannelIndex {
        return RebuildChannelIndex(
            channelRepository,
            channelsIndex
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
            videoChannelService,
            legacyVideoSearchService
        )
    }

    @Bean
    fun videoAnalysisService(): VideoAnalysisService {
        return VideoAnalysisService(videoRepository, eventBus, playbackRepository)
    }

    @Bean
    fun videoPlaybackService(playbackUpdateService: PlaybackUpdateService): UpdateYoutubePlayback {
        return UpdateYoutubePlayback(playbackUpdateService)
    }

    @Bean
    fun getSubject(): GetSubject {
        return GetSubject(subjectRepository)
    }

    @Bean
    fun deleteSubject(): DeleteSubject {
        return DeleteSubject(subjectService)
    }

    @Bean
    fun getSubjects(): GetSubjects {
        return GetSubjects(subjectRepository)
    }

    @Bean
    fun createSubject(): CreateSubject {
        return CreateSubject(subjectRepository, subjectIndex)
    }

    @Bean
    fun updateSubject(): UpdateSubject {
        return UpdateSubject(subjectService)
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
    fun stringToDurationConverter(): QueryConverter {
        return QueryConverter()
    }

    @Bean
    fun broadcastVideos(): BroadcastVideos {
        return BroadcastVideos(videoRepository, eventBus)
    }

    @Bean
    fun broadcastCollections(): BroadcastCollections {
        return BroadcastCollections(collectionRepository, eventBus)
    }

    @Bean
    fun videoUpdateService(): VideoIndexUpdater {
        return VideoIndexUpdater(
            videoRepository,
            videoChannelService,
            videoIndex,
            legacyVideoSearchService,
            organisationService,
            priceComputingService
        )
    }

    @Bean
    fun contentPartnerUpdated(): ChannelUpdated {
        return ChannelUpdated(videoRepository)
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
    fun getVideoById(videoRetrievalService: VideoRetrievalService): GetVideoById {
        return GetVideoById(videoRetrievalService)
    }

    @Bean
    fun getVideoPrice(userService: UserService, priceComputingService: PriceComputingService): GetVideoPrice {
        return GetVideoPrice(userService, priceComputingService)
    }

    @Bean
    fun getVideosByQuery(
        queryConverter: QueryConverter,
        retrievePlayableVideos: RetrievePlayableVideos
    ): GetVideosByQuery {
        return GetVideosByQuery(
            retrievePlayableVideos,
            eventService,
            userService,
            queryConverter
        )
    }

    @Bean
    fun getVideoUrlAssets(
        searchVideo: SearchVideo,
        playbackProvider: KalturaPlaybackProvider
    ): GetVideoUrlAssets = GetVideoUrlAssets(searchVideo, playbackProvider)

    @Bean
    fun getContentWarning() = GetContentWarning(contentWarningRepository)

    @Bean
    fun getAllContentWarnings() = GetAllContentWarnings(contentWarningRepository)

    @Bean
    fun createContentWarning() = CreateContentWarning(contentWarningRepository)

    @Bean
    fun getAttachmentTypes() = GetAttachmentTypes()

    @Bean
    fun videoRetrievalService(
        videoRepository: VideoRepository,
        videoIndex: VideoIndex
    ): VideoRetrievalService {
        return VideoRetrievalService(videoRepository, videoIndex)
    }

    @Bean
    fun retrievePlayableVideos(
        videoRepository: VideoRepository,
        videoIndex: VideoIndex
    ): RetrievePlayableVideos {
        return RetrievePlayableVideos(videoRepository, videoIndex)
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
}
