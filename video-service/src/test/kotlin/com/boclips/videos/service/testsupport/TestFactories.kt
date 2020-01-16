package com.boclips.videos.service.testsupport

import com.boclips.eventbus.domain.video.Captions
import com.boclips.eventbus.domain.video.CaptionsFormat
import com.boclips.eventbus.domain.video.VideoAnalysedKeyword
import com.boclips.eventbus.domain.video.VideoAnalysedTopic
import com.boclips.eventbus.events.video.VideoAnalysed
import com.boclips.users.client.model.TeacherPlatformAttributes
import com.boclips.videos.api.request.video.PlaybackResource
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.RequestContext
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CollectionUpdateResult
import com.boclips.videos.service.domain.model.discipline.Discipline
import com.boclips.videos.service.domain.model.discipline.DisciplineId
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Dimensions
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSubjects
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.boclips.videos.service.infrastructure.video.PlaybackDocument
import com.boclips.videos.service.infrastructure.video.VideoAssetDocument
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import org.bson.types.ObjectId
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale
import com.boclips.security.utils.User as SecurityUser

object TestFactories {

    fun createVideoId() = VideoId(aValidId())

    fun createVideo(
        videoId: String = createVideoId().value,
        title: String = "title",
        description: String = "description",
        contentPartnerName: String = "Reuters",
        contentPartnerId: ContentPartnerId = ContentPartnerId(
            value = ObjectId().toHexString()
        ),
        contentPartnerVideoId: String = "cp-id-$videoId",
        playback: VideoPlayback = createKalturaPlayback(),
        type: ContentType = ContentType.INSTRUCTIONAL_CLIPS,
        keywords: List<String> = listOf("keyword"),
        subjects: Set<Subject> = emptySet(),
        subjectsSetManually: Boolean? = null,
        releasedOn: LocalDate = LocalDate.parse("2018-01-01"),
        ingestedAt: ZonedDateTime = ZonedDateTime.now(),
        legalRestrictions: String = "",
        language: Locale? = null,
        transcript: String? = null,
        topics: Set<Topic> = emptySet(),
        ageRange: AgeRange = AgeRange.bounded(5, 12),
        ratings: List<UserRating> = emptyList(),
        tags: List<UserTag> = emptyList(),
        contentPartner: ContentPartner = ContentPartner(
            contentPartnerId = contentPartnerId,
            name = contentPartnerName
        ),
        videoReference: String = contentPartnerVideoId,
        promoted: Boolean? = null
    ): Video {
        return Video(
            videoId = VideoId(value = ObjectId(videoId).toHexString()),
            playback = playback,
            title = title,
            description = description,
            keywords = keywords,
            releasedOn = releasedOn,
            ingestedAt = ingestedAt,
            type = type,
            legalRestrictions = legalRestrictions,
            subjects = VideoSubjects(
                items = subjects,
                setManually = subjectsSetManually
            ),
            language = language,
            transcript = transcript,
            topics = topics,
            ageRange = ageRange,
            contentPartner = contentPartner,
            videoReference = videoReference,
            ratings = ratings,
            tags = tags,
            promoted = promoted
        )
    }

    fun createSubject(id: String = aValidId(), name: String = id): Subject =
        Subject(
            id = SubjectId(id), name = name
        )

    fun createUserTag(id: String = aValidId(), label: String = id, userId: String = "user id"): UserTag =
        UserTag(
            tag = createTag(id, label),
            userId = UserId(userId)
        )

    fun createTag(id: String = aValidId(), label: String = id): Tag =
        Tag(
            id = TagId(id), label = label
        )

    object CollectionsRequestFactory {
        fun sample(
            query: String? = null,
            subjects: List<String> = emptyList(),
            owner: String? = null,
            page: Int = 0,
            size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE,
            public: Boolean? = null,
            bookmarked: Boolean? = null
        ): CollectionsController.CollectionsRequest {
            return CollectionsController.CollectionsRequest(
                query = query,
                subject = if (subjects.isNotEmpty()) subjects.joinToString(",") else null,
                public = public,
                owner = owner,
                page = page,
                size = size,
                bookmarked = bookmarked
            )
        }

        fun unfiltered() = sample()
    }

    fun createKalturaPlayback(
        entryId: String = "entry-id",
        assets: Set<VideoAsset>? = emptySet(),
        duration: Duration = Duration.ofSeconds(11),
        downloadUrl: String = "kaltura-download",
        createdAt: ZonedDateTime? = null,
        referenceId: String = "555",
        originalDimensions: Dimensions? = Dimensions(width = 360, height = 480)
    ): StreamPlayback {
        return StreamPlayback(
            id = PlaybackId(type = PlaybackProviderType.KALTURA, value = entryId),
            referenceId = referenceId,
            downloadUrl = downloadUrl,
            createdAt = createdAt,
            duration = duration,
            assets = assets,
            originalDimensions = originalDimensions
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
            duration = duration,
            thumbnailUrl = thumbnailUrl
        )
    }

    fun createCollection(
        id: CollectionId = CollectionId("collection-id"),
        owner: String = "collection owner",
        title: String = "collection title",
        videos: List<VideoId> = listOf(createVideo().videoId),
        createdAt: ZonedDateTime = ZonedDateTime.now(),
        updatedAt: ZonedDateTime = ZonedDateTime.now(),
        isPublic: Boolean = false,
        createdByBoclips: Boolean = false,
        bookmarks: Set<UserId> = emptySet(),
        subjects: Set<Subject> = emptySet(),
        ageRangeMin: Int = 0,
        ageRangeMax: Int = 10,
        description: String = "collection description",
        attachments: Set<Attachment> = emptySet()
    ) = Collection(
        id = id,
        owner = UserId(value = owner),
        title = title,
        videos = videos,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPublic = isPublic,
        createdByBoclips = createdByBoclips,
        bookmarks = bookmarks,
        subjects = subjects,
        ageRange = AgeRange.bounded(ageRangeMin, ageRangeMax),
        description = description,
        attachments = attachments
    )

    fun createCollectionUpdateResult(
        collectionId: CollectionId = CollectionId(aValidId()),
        collection: Collection = createCollection(id = collectionId),
        command: CollectionUpdateCommand = CollectionUpdateCommand.RenameCollection(
            collectionId,
            "collection title",
            UserFactory.sample()
        )
    ): CollectionUpdateResult {
        return CollectionUpdateResult(
            collection = collection,
            commands = listOf(command)
        )
    }

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

    fun createKalturaPlaybackDocument(
        id: String = "valid-id",
        entryId: String? = "entry-id",
        downloadUrl: String? = null,
        lastVerified: Instant? = null,
        duration: Int? = null,
        originalWidth: Int? = null,
        originalHeight: Int? = null,
        assets: List<VideoAssetDocument>? = emptyList()
    ): PlaybackDocument {
        return PlaybackDocument(
            type = "KALTURA",
            id = id,
            entryId = entryId,
            thumbnailUrl = null,
            downloadUrl = downloadUrl,
            lastVerified = lastVerified,
            duration = duration,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            assets = assets
        )
    }

    fun createYoutubePlaybackDocument(
        id: String = "valid-id",
        duration: Int? = null
    ): PlaybackDocument {
        return PlaybackDocument(
            type = "YOUTUBE",
            id = id,
            entryId = null,
            thumbnailUrl = null,
            downloadUrl = null,
            lastVerified = null,
            duration = duration,
            originalWidth = null,
            originalHeight = null,
            assets = null
        )
    }

    fun createContentPartner(
        id: ContentPartnerId = ContentPartnerId(
            ObjectId().toHexString()
        ),
        name: String = "TED"
    ): ContentPartner {
        return ContentPartner(
            contentPartnerId = id,
            name = name
        )
    }

    fun createSubjectDocument(name: String): SubjectDocument {
        return SubjectDocument(id = ObjectId(), name = name)
    }
}

object UserRatingFactory {
    fun sample(
        rating: Int = 3, userId: UserId = UserId(
            "me"
        )
    ): UserRating =
        UserRating(rating, userId)
}

object PlaybackResourceFactory {
    fun sample(type: String = "STREAM", downloadUrl: String? = "download-url") =
        StreamPlaybackResource(
            id = "playback-id",
            type = type,
            downloadUrl = downloadUrl,
            duration = Duration.ZERO,
            referenceId = "reference-id",
            _links = null
        )
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
        playback: PlaybackResource? = PlaybackResourceFactory.sample(),
        type: VideoTypeResource = VideoTypeResourceFactory.sample(),
        subjects: Set<SubjectResource> = emptySet(),
        releasedOn: LocalDate = LocalDate.parse("2018-01-01"),
        legalRestrictions: String = "",
        contentPartner: String = "Content Partner",
        badges: Set<String> = emptySet(),
        hasTranscripts: Boolean = true,
        rating: Double? = null
    ) = VideoResource(
        id = id,
        playback = playback,
        title = title,
        description = description,
        releasedOn = releasedOn,
        contentPartnerVideoId = contentPartnerVideoId,
        type = type,
        legalRestrictions = legalRestrictions,
        subjects = subjects,
        contentPartner = contentPartner,
        badges = badges,
        hasTranscripts = hasTranscripts,
        rating = rating,
        _links = null
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

object AttachmentFactory {
    fun sample(
        id: String = ObjectId().toHexString(),
        description: String = "description",
        linkToResource: String = "https://example.com",
        type: AttachmentType = AttachmentType.LESSON_PLAN
    ) = Attachment(
        attachmentId = AttachmentId(id),
        description = description,
        linkToResource = linkToResource,
        type = type
    )

    fun sampleWithLessonPlan(
        id: String = ObjectId().toHexString(),
        description: String = "description",
        linkToResource: String = "https://example.com",
        type: AttachmentType = AttachmentType.LESSON_PLAN
    ) = Attachment(
        attachmentId = AttachmentId(id),
        description = description,
        linkToResource = linkToResource,
        type = type
    )
}

object AccessRulesFactory {
    fun sample(
        collectionAccessRule: CollectionAccessRule = CollectionAccessRule.everything(),
        videoAccessRule: VideoAccessRule = VideoAccessRule.Everything
    ): AccessRules {
        return AccessRules(
            collectionAccess = collectionAccessRule,
            videoAccess = videoAccessRule
        )
    }

    fun publicOnly() =
        sample(collectionAccessRule = CollectionAccessRule.public())

    fun specificIds(vararg collectionIds: CollectionId) =
        sample(collectionAccessRule = CollectionAccessRule.specificIds(collectionIds.toList()))

    fun superuser(): AccessRules =
        sample(collectionAccessRule = CollectionAccessRule.everything())

    fun asOwner(ownerId: String): AccessRules =
        sample(
            collectionAccessRule = CollectionAccessRule.asOwner(
                UserId(
                    ownerId
                )
            )
        )
}

object SecurityUserFactory {
    fun sample(
        roles: Set<String> = emptySet(),
        id: String = "some-id",
        boclipsEmployee: Boolean = false
    ): SecurityUser {
        return SecurityUser(
            boclipsEmployee = boclipsEmployee,
            id = id,
            authorities = roles.map { "ROLE_$it" }.toSet()
        )
    }

    fun createClientUser(
        id: String = "user-id",
        organisationAccountId: String = "organisation-id",
        subjects: List<com.boclips.users.client.model.Subject> = emptyList(),
        teacherPlatformAttributes: TeacherPlatformAttributes? = TeacherPlatformAttributes(null)
    ): com.boclips.users.client.model.User {
        return com.boclips.users.client.model.User(
            id,
            organisationAccountId,
            subjects,
            teacherPlatformAttributes
        )
    }
}

object UserFactory {
    fun sample(
        id: String = "userio-123",
        boclipsEmployee: Boolean = false,
        isPermittedToViewAnyCollection: Boolean = false,
        isPermittedToShareVideo: Boolean = false,
        eventId: String? = null,
        accessRulesSupplier: (user: User) -> AccessRules = {
            AccessRules(
                videoAccess = VideoAccessRule.Everything,
                collectionAccess = CollectionAccessRule.PublicOnly
            )
        }
    ): User {
        return User(
            id = UserId(id),
            isAuthenticated = true,
            isBoclipsEmployee = boclipsEmployee,
            context = RequestContext(origin = "https://teachers.boclips.com"),
            isPermittedToUpdateVideo = true,
            isPermittedToViewAnyCollection = isPermittedToViewAnyCollection,
            isPermittedToRateVideos = true,
            isPermittedToShareVideo = isPermittedToShareVideo,
            eventId = eventId?.let(::UserId),
            accessRulesSupplier = accessRulesSupplier
        )
    }
}

object CreatePlaybackEventCommandFactory {
    fun sample(
        videoId: String = TestFactories.aValidId(),
        videoIndex: Int = 0,
        segmentStartSeconds: Long = 0,
        segmentEndSeconds: Long = 0,
        captureTime: ZonedDateTime? = null
    ): CreatePlaybackEventCommand {
        return CreatePlaybackEventCommand(
            videoId = videoId,
            videoIndex = videoIndex,
            segmentStartSeconds = segmentStartSeconds,
            segmentEndSeconds = segmentEndSeconds,
            captureTime = captureTime
        )
    }
}

