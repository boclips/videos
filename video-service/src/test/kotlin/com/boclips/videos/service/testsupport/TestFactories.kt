package com.boclips.videos.service.testsupport

import com.boclips.events.types.Captions
import com.boclips.events.types.CaptionsFormat
import com.boclips.events.types.video.VideoAnalysed
import com.boclips.events.types.video.VideoAnalysedKeyword
import com.boclips.events.types.video.VideoAnalysedTopic
import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.kalturaclient.media.streams.StreamUrls
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.disciplines.Discipline
import com.boclips.videos.service.domain.model.disciplines.DisciplineId
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.subjects.Subject
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.video.mongo.PlaybackDocument
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.subject.SubjectResource
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoResourceDeliveryMethod
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.presentation.video.VideoTypeResource
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import org.bson.types.ObjectId
import org.springframework.hateoas.Resource
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

object TestFactories {

    fun createVideo(
        videoId: String = ObjectId().toHexString(),
        title: String = "title",
        description: String = "description",
        contentPartnerName: String = "Reuters",
        contentPartnerId: ContentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
        contentPartnerVideoId: String = "cp-id-$videoId",
        playback: VideoPlayback = createKalturaPlayback(),
        type: LegacyVideoType = LegacyVideoType.INSTRUCTIONAL_CLIPS,
        keywords: List<String> = listOf("keyword"),
        subjects: Set<Subject> = emptySet(),
        releasedOn: LocalDate = LocalDate.parse("2018-01-01"),
        legalRestrictions: String = "",
        language: Locale? = null,
        transcript: String? = null,
        topics: Set<Topic> = emptySet(),
        searchable: Boolean = true,
        hiddenFromSearchForDeliveryMethods: Set<DeliveryMethod> = emptySet(),
        ageRange: AgeRange = AgeRange.bounded(5, 12),
        rating: UserRating? = null,
        contentPartner: ContentPartner = ContentPartner(
            contentPartnerId = contentPartnerId,
            name = contentPartnerName,
            ageRange = ageRange,
            credit = Credit.PartnerCredit,
            searchable = true
        ),
        videoReference: String = contentPartnerVideoId
    ): Video {
        return Video(
            videoId = VideoId(value = ObjectId(videoId).toHexString()),
            playback = playback,
            title = title,
            description = description,
            keywords = keywords,
            releasedOn = releasedOn,
            type = type,
            legalRestrictions = legalRestrictions,
            subjects = subjects,
            language = language,
            transcript = transcript,
            topics = topics,
            searchable = searchable,
            hiddenFromSearchForDeliveryMethods = hiddenFromSearchForDeliveryMethods,
            ageRange = ageRange,
            contentPartner = contentPartner,
            videoReference = videoReference,
            rating = rating
        )
    }

    fun createSubject(id: String = aValidId(), name: String = id): Subject =
        Subject(
            id = SubjectId(id), name = name
        )

    fun createMediaEntry(
        id: String = "1",
        referenceId: String = "ref-id-$id",
        duration: Duration = Duration.ofMinutes(1),
        status: MediaEntryStatus = MediaEntryStatus.READY
    ): MediaEntry? {
        return MediaEntry.builder()
            .id(id)
            .referenceId(referenceId)
            .streams(StreamUrls("https://stream/[FORMAT]/video-$id.mp4"))
            .thumbnailUrl("https://thumbnail/thumbnail-$id.mp4")
            .downloadUrl("https://download/video-$id.mp4")
            .duration(duration)
            .status(status)
            .build()
    }

    fun createKalturaPlayback(
        duration: Duration = Duration.ofSeconds(11),
        downloadUrl: String = "kaltura-download",
        playbackId: String = "555",
        thumbnailUrl: String = "kaltura-thumbnailUrl",
        hlsStreamUrl: String = "hls-stream",
        dashStreamUrl: String = "dash-stream",
        progressiveStreamUrl: String = "progressive-stream"
    ): StreamPlayback {
        return StreamPlayback(
            id = PlaybackId(type = PlaybackProviderType.KALTURA, value = playbackId),
            appleHlsStreamUrl = hlsStreamUrl,
            mpegDashStreamUrl = dashStreamUrl,
            progressiveDownloadStreamUrl = progressiveStreamUrl,
            thumbnailUrl = thumbnailUrl,
            downloadUrl = downloadUrl,
            duration = duration
        )
    }

    fun createYoutubePlayback(
        playbackId: PlaybackId = PlaybackId(
            type = PlaybackProviderType.YOUTUBE,
            value = "444"
        ),
        duration: Duration = Duration.ofSeconds(21),
        thumbnailUrl: String = "youtube-thumbnail"
    ): YoutubePlayback {
        return YoutubePlayback(
            id = playbackId,
            thumbnailUrl = thumbnailUrl,
            duration = duration
        )
    }

    fun createCreateVideoRequest(
        provider: String? = "AP",
        providerVideoId: String? = "AP-1",
        title: String? = "an AP video",
        description: String? = "an AP video about penguins",
        releasedOn: LocalDate? = LocalDate.now(),
        legalRestrictions: String? = "None",
        keywords: List<String>? = listOf("k1", "k2"),
        videoType: String? = "NEWS",
        playbackId: String? = "123",
        playbackProvider: String? = "KALTURA",
        analyseVideo: Boolean = false,
        searchable: Boolean? = true,
        hiddenFromSearchForDeliveryMethods: Set<VideoResourceDeliveryMethod>? = emptySet(),
        subjects: Set<String> = setOf()
    ) = CreateVideoRequest(
        provider = provider,
        providerVideoId = providerVideoId,
        title = title,
        description = description,
        releasedOn = releasedOn,
        legalRestrictions = legalRestrictions,
        keywords = keywords,
        videoType = videoType,
        playbackId = playbackId,
        playbackProvider = playbackProvider,
        analyseVideo = analyseVideo,
        searchable = searchable,
        hiddenFromSearchForDeliveryMethods = hiddenFromSearchForDeliveryMethods,
        subjects = subjects
    )

    fun createCollectionRequest(
        title: String? = "collection title",
        videos: List<String> = listOf(),
        public: Boolean? = null
    ) = CreateCollectionRequest(
        title = title,
        videos = videos,
        public = public
    )

    fun createCollection(
        id: CollectionId = CollectionId("collection-id"),
        owner: String = "collection owner",
        title: String = "collection title",
        videos: List<VideoId> = listOf(createVideo().videoId),
        updatedAt: Instant = Instant.now(),
        isPublic: Boolean = false,
        createdByBoclips: Boolean = false,
        bookmarks: Set<UserId> = emptySet(),
        subjects: Set<SubjectId> = emptySet(),
        viewerIds: List<UserId> = emptyList()
    ) = Collection(
        id = id,
        owner = UserId(value = owner),
        viewerIds = viewerIds,
        title = title,
        videos = videos,
        updatedAt = updatedAt,
        isPublic = isPublic,
        createdByBoclips = createdByBoclips,
        bookmarks = bookmarks,
        subjects = subjects,
        ageRange = AgeRange.unbounded()
    )

    fun createCollectionResource(
        id: String = "collection-id",
        owner: String = "collection owner",
        title: String = "collection title",
        videos: List<Resource<VideoResource>> = emptyList(),
        updatedAt: Instant = Instant.now(),
        isPublic: Boolean = false,
        isBookmarked: Boolean = false,
        isMine: Boolean = false,
        createdBy: String = "Johnny Bravo",
        subjects: Set<Resource<SubjectResource>> = emptySet(),
        ageRange: AgeRangeResource? = null
    ) = CollectionResource(
        id = id,
        owner = owner,
        title = title,
        videos = videos,
        updatedAt = updatedAt,
        public = isPublic,
        mine = isMine,
        bookmarked = isBookmarked,
        createdBy = createdBy,
        subjects = subjects,
        ageRange = ageRange
    )

    fun aValidId(): String {
        return ObjectId().toHexString()
    }

    fun createCaptions(
        language: Locale = Locale.US,
        autoGenerated: Boolean = true,
        format: CaptionsFormat = CaptionsFormat.VTT,
        content: String = "captions content"
    ): Captions {
        return Captions.builder()
            .language(language)
            .format(format)
            .autoGenerated(autoGenerated)
            .content(content)
            .build()
    }

    fun createVideoAnalysed(
        videoId: String = aValidId(),
        language: Locale = Locale.US,
        transcript: String = "the transcript",
        topics: List<VideoAnalysedTopic> = emptyList(),
        keywords: List<VideoAnalysedKeyword> = emptyList()
    ): VideoAnalysed {
        return VideoAnalysed
            .builder()
            .videoId(videoId)
            .language(language)
            .topics(topics)
            .keywords(keywords)
            .transcript(transcript)
            .captions(createCaptions())
            .build()
    }

    fun createVideoAnalysedTopic(
        name: String = "Statistics",
        language: Locale = Locale.UK,
        confidence: Double = 0.7,
        parent: VideoAnalysedTopic? = null
    ): VideoAnalysedTopic {
        return VideoAnalysedTopic.builder()
            .confidence(confidence)
            .segments(emptyList())
            .language(language)
            .parent(parent)
            .name(name)
            .build()
    }

    fun createVideoAnalysedKeyword(
        name: String = "pH"
    ): VideoAnalysedKeyword {
        return VideoAnalysedKeyword.builder()
            .name(name)
            .confidence(0.5)
            .language(Locale.US)
            .segments(emptyList())
            .build()
    }

    fun createKalturaCaptionAsset(
        language: KalturaLanguage = KalturaLanguage.ENGLISH,
        label: String = language.getName()
    ): CaptionAsset {
        return CaptionAsset.builder()
            .language(language)
            .label(label)
            .build()
    }

    fun createUpdateCollectionRequest(
        title: String = "collection title",
        isPublic: Boolean = true,
        subjects: Set<String> = emptySet(),
        ageRange: AgeRangeRequest = AgeRangeRequest(min = 3, max = 5)
    ): UpdateCollectionRequest {
        return UpdateCollectionRequest(
            title = title,
            isPublic = isPublic,
            subjects = subjects,
            ageRange = ageRange
        )
    }

    fun createKalturaPlaybackDocument(
        id: String = "valid-id",
        thumbnailUrl: List<String>? = listOf("thumbnail.com"),
        hlsStreamUrl: String? = null,
        dashStreamUrl: String? = null,
        progressiveStreamUrl: String? = null,
        downloadUrl: String? = null,
        lastVerified: Instant? = null,
        duration: Int? = null
    ): PlaybackDocument {
        return PlaybackDocument(
            type = "KALTURA",
            id = id,
            thumbnailUrl = thumbnailUrl,
            downloadUrl = downloadUrl,
            hlsStreamUrl = hlsStreamUrl,
            dashStreamUrl = dashStreamUrl,
            progressiveStreamUrl = progressiveStreamUrl,
            lastVerified = lastVerified,
            duration = duration
        )
    }

    fun createYoutubePlaybackDocument(
        id: String = "valid-id",
        duration: Int? = null
    ): PlaybackDocument {
        return PlaybackDocument(
            type = "YOUTUBE",
            id = id,
            thumbnailUrl = null,
            downloadUrl = null,
            hlsStreamUrl = null,
            dashStreamUrl = null,
            progressiveStreamUrl = null,
            lastVerified = null,
            duration = duration
        )
    }

    fun createContentPartner(
        id: ContentPartnerId = ContentPartnerId(ObjectId().toHexString()),
        name: String = "TED",
        ageRange: AgeRange = AgeRange.bounded(5, 11),
        credit: Credit = Credit.PartnerCredit,
        searchable: Boolean = true
    ): ContentPartner {
        return ContentPartner(
            contentPartnerId = id,
            name = name,
            ageRange = ageRange,
            credit = credit,
            searchable = searchable
        )
    }

    fun createContentPartnerRequest(
        name: String? = "TED",
        ageRange: AgeRangeRequest? = AgeRangeRequest(
            min = 5,
            max = 11
        ),
        accreditedToYtChannel: String? = null,
        searchable: Boolean = true
    ): ContentPartnerRequest {
        return ContentPartnerRequest(
            name = name,
            ageRange = ageRange,
            accreditedToYtChannelId = accreditedToYtChannel,
            searchable = searchable
        )
    }
}

object PlaybackResourceFactory {
    fun sample(type: String = "STREAM") =
        StreamPlaybackResource(type, "http://example.com", "thumbnail-url", Duration.ZERO, streamUrl = "stream-url")
}

object VideoTypeResourceFactory {
    fun sample(id: Int = 0, name: String = "name") = VideoTypeResource(id, name)
}

object VideoResourceFactory {
    fun sample(
        id: String = ObjectId().toHexString(),
        title: String = "title",
        description: String = "description",
        contentPartnerVideoId: String = "cp-id-$id",
        playback: PlaybackResource = PlaybackResourceFactory.sample(),
        type: VideoTypeResource = VideoTypeResourceFactory.sample(),
        subjects: Set<String> = emptySet(),
        releasedOn: LocalDate = LocalDate.parse("2018-01-01"),
        legalRestrictions: String = "",
        contentPartner: String = "Content Partner",
        badges: Set<String> = emptySet(),
        status: VideoResourceStatus = VideoResourceStatus.SEARCHABLE,
        hasTranscripts: Boolean = true
    ) = VideoResource(
        id = id,
        playback = Resource(playback),
        title = title,
        description = description,
        releasedOn = releasedOn,
        contentPartnerVideoId = contentPartnerVideoId,
        type = type,
        legalRestrictions = legalRestrictions,
        subjects = subjects,
        contentPartner = contentPartner,
        badges = badges,
        status = status,
        hasTranscripts = hasTranscripts
    )
}

object SubjectFactory {
    fun sample(
        id: String = ObjectId().toHexString(),
        name: String = "name"
    ) = Subject(
        id = SubjectId(id),
        name = name
    )
}

object DisciplineFactory {
    fun sample(
        id: String = ObjectId().toHexString(),
        code: String = "code",
        name: String = "name",
        subjects: List<Subject> = listOf(SubjectFactory.sample())
    ) = Discipline(
        id = DisciplineId(id),
        code = code,
        name = name,
        subjects = subjects
    )
}
