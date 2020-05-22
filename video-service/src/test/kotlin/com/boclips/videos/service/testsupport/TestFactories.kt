package com.boclips.videos.service.testsupport

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractCosts
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractDates
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRestrictions
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRoyaltySplit
import com.boclips.eventbus.domain.video.Captions
import com.boclips.eventbus.domain.video.CaptionsFormat
import com.boclips.eventbus.domain.video.VideoAnalysedKeyword
import com.boclips.eventbus.domain.video.VideoAnalysedTopic
import com.boclips.eventbus.events.video.VideoAnalysed
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.contract.ContentPartnerContractCostsRequest
import com.boclips.videos.api.request.contract.ContentPartnerContractRestrictionsRequest
import com.boclips.videos.api.request.contract.CreateContractRequest
import com.boclips.videos.api.request.video.PlaybackResource
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import com.boclips.videos.api.response.contract.ContentPartnerContractRoyaltySplitResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.playback.Dimensions
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
import com.boclips.videos.service.domain.model.user.RequestContext
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSubjects
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartner
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import com.boclips.videos.service.infrastructure.collection.CollectionUpdateResult
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.boclips.videos.service.infrastructure.video.PlaybackDocument
import com.boclips.videos.service.infrastructure.video.VideoAssetDocument
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Currency
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
        ageRange: AgeRange = AgeRange.of(min = 5, max = 12, curatedManually = false),
        ratings: List<UserRating> = emptyList(),
        tags: List<UserTag> = emptyList(),
        contentPartner: ContentPartner = ContentPartner(
            contentPartnerId = contentPartnerId,
            name = contentPartnerName
        ),
        videoReference: String = contentPartnerVideoId,
        promoted: Boolean? = null,
        attachments: List<Attachment> = emptyList(),
        contentWarnings: List<ContentWarning> = emptyList()
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
            promoted = promoted,
            attachments = attachments,
            contentWarnings = contentWarnings
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

    fun createKalturaPlayback(
        entryId: String = "entry-id",
        assets: Set<VideoAsset>? = emptySet(),
        duration: Duration = Duration.ofSeconds(11),
        downloadUrl: String = "kaltura-download",
        thumbnailSecond: Int? = null,
        createdAt: ZonedDateTime? = null,
        referenceId: String = "555",
        originalDimensions: Dimensions? = Dimensions(
            width = 360,
            height = 480
        )
    ): StreamPlayback {
        return StreamPlayback(
            id = PlaybackId(type = PlaybackProviderType.KALTURA, value = entryId),
            referenceId = referenceId,
            downloadUrl = downloadUrl,
            thumbnailSecond = thumbnailSecond,
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
        discoverable: Boolean = false,
        promoted: Boolean = false,
        createdByBoclips: Boolean = false,
        bookmarks: Set<UserId> = emptySet(),
        subjects: Set<Subject> = emptySet(),
        ageRangeMin: Int = 0,
        ageRangeMax: Int = 10,
        description: String = "collection description",
        attachments: Set<Attachment> = emptySet(),
        units: List<Collection> = emptyList()
    ) = Collection(
        id = id,
        owner = UserId(value = owner),
        title = title,
        videos = videos,
        createdAt = createdAt,
        updatedAt = updatedAt,
        discoverable = discoverable,
        promoted = promoted,
        createdByBoclips = createdByBoclips,
        bookmarks = bookmarks,
        subjects = subjects,
        ageRange = AgeRange.of(min = ageRangeMin, max = ageRangeMax, curatedManually = true),
        description = description,
        attachments = attachments,
        units = units,
        default = false
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
        thumbnailSecond: Int? = null,
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
            thumbnailSecond = thumbnailSecond,
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
            thumbnailSecond = null,
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
    fun sample(id: String = "playback-id", type: String = "STREAM", downloadUrl: String? = "download-url") =
        StreamPlaybackResource(
            id = id,
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
        subjects: Set<com.boclips.videos.api.response.subject.SubjectResource> = emptySet(),
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
        videoAccess: VideoAccess = VideoAccess.Everything
    ): AccessRules {
        return AccessRules(
            collectionAccess = collectionAccessRule,
            videoAccess = videoAccess
        )
    }

    fun everything() =
        sample(collectionAccessRule = CollectionAccessRule.everything())

    fun specificIds(vararg collectionIds: CollectionId) =
        sample(collectionAccessRule = CollectionAccessRule.specificIds(collectionIds.toList()))

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
}

object UserFactory {
    fun sample(
        id: String = "userio-123",
        boclipsEmployee: Boolean = false,
        isPermittedToViewAnyCollection: Boolean = false,
        overrideIdSupplier: () -> String? = { null },
        isAuthenticated: Boolean = true,
        accessRulesSupplier: (user: User) -> AccessRules = {
            AccessRules(
                videoAccess = VideoAccess.Everything,
                collectionAccess = CollectionAccessRule.Everything
            )
        }
    ): User {
        return User(
            id = UserId(id),
            isAuthenticated = isAuthenticated,
            isBoclipsEmployee = boclipsEmployee,
            context = RequestContext(origin = "https://teachers.boclips.com"),
            isPermittedToUpdateVideo = true,
            isPermittedToModifyAnyCollection = isPermittedToViewAnyCollection,
            isPermittedToRateVideos = true,
            isPermittedToViewCollections = true,
            overrideIdSupplier = { overrideIdSupplier()?.let(::UserId) },
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

object ContentPartnerContractFactory {
    fun sample(
        id: String? = null,
        contentPartnerName: String? = null,
        contractDocument: String? = "http://contractdocument.com",
        contractDates: ContractDates? = ContractDates(
            LocalDate.of(2011, 10, 10),
            LocalDate.of(2012, 10, 31)
        ),
        contractIsRolling: Boolean? = true,
        daysBeforeTerminationWarning: Int? = 30,
        yearsForMaximumLicense: Int? = 5,
        daysForSellOffPeriod: Int? = 60,
        royaltySplit: ContractRoyaltySplit? =
            ContractRoyaltySplit(
                download = 10.1.toFloat(),
                streaming = 20.5.toFloat()
            ),
        minimumPriceDescription: String? = "This is the minimum price",
        remittanceCurrency: String? = "GBP",
        restrictions: ContractRestrictions = ContractRestrictions(
            clientFacing = listOf("restriction 1", "restriction 2"),
            territory = "Australia",
            licensing = "License 1",
            editing = "Edit",
            marketing = "Marketing info",
            companies = "Companies info",
            payout = "Payout info",
            other = "Other info"
        ),
        costs: ContractCosts = ContractCosts(
            minimumGuarantee = listOf(BigDecimal.ONE),
            upfrontLicense = BigDecimal.ONE,
            technicalFee = BigDecimal.ONE,
            recoupable = true
        )
    ) =
        ContentPartnerContract(
            id = ContentPartnerContractId(
                id ?: ObjectId().toHexString()
            ),
            contentPartnerName = contentPartnerName ?: "content-partner-name",
            contractDocument = URL(contractDocument),
            contractDates = contractDates,
            contractIsRolling = contractIsRolling,
            daysBeforeTerminationWarning = daysBeforeTerminationWarning,
            yearsForMaximumLicense = yearsForMaximumLicense,
            daysForSellOffPeriod = daysForSellOffPeriod,
            royaltySplit = royaltySplit,
            minimumPriceDescription = minimumPriceDescription,
            remittanceCurrency = remittanceCurrency?.let { Currency.getInstance(remittanceCurrency) },
            restrictions = restrictions,
            costs = costs
        )

    fun contentPartnerContractRequest(
        contentPartnerName: String? = null,
        contractDocument: String? = "http://contractdocument.com",
        contractDates: ContractDates? = ContractDates(
            LocalDate.of(2011, 10, 10),
            LocalDate.of(2012, 10, 31)
        ),
        contractIsRolling: Boolean? = true,
        daysBeforeTerminationWarning: Int? = 30,
        yearsForMaximumLicense: Int? = 5,
        daysForSellOffPeriod: Int? = 60,
        royaltySplit: ContractRoyaltySplit? =
            ContractRoyaltySplit(
                download = 10.1.toFloat(),
                streaming = 20.5.toFloat()
            ),
        minimumPriceDescription: String? = "This is the minimum price",
        remittanceCurrency: String? = "GBP",
        restrictions: ContractRestrictions = ContractRestrictions(
            clientFacing = listOf("restriction 1", "restriction 2"),
            territory = "Australia",
            licensing = "License 1",
            editing = "Edit",
            marketing = "Marketing info",
            companies = "Companies info",
            payout = "Payout info",
            other = "Other info"
        ),
        costs: ContractCosts = ContractCosts(
            minimumGuarantee = listOf(BigDecimal.ONE),
            upfrontLicense = BigDecimal.ONE,
            technicalFee = BigDecimal.ONE,
            recoupable = true
        )
    ) = CreateContractRequest(
        contentPartnerName = contentPartnerName ?: "content-partner-name",
        contractDocument = contractDocument?.let { Specified(value = it) },
        contractDates = contractDates.let {
            ContentPartnerContractDatesResource(
                start = it?.start.toString(),
                end = it?.end.toString()
            )
        },
        contractIsRolling = contractIsRolling,
        daysBeforeTerminationWarning = daysBeforeTerminationWarning,
        yearsForMaximumLicense = yearsForMaximumLicense,
        daysForSellOffPeriod = daysForSellOffPeriod,
        royaltySplit = royaltySplit.let {
            ContentPartnerContractRoyaltySplitResource(
                download = it?.download,
                streaming = it?.streaming
            )
        },
        minimumPriceDescription = minimumPriceDescription,
        remittanceCurrency = remittanceCurrency.toString(),
        restrictions = restrictions.let {
            ContentPartnerContractRestrictionsRequest(
                clientFacing = it.clientFacing,
                territory = it.territory,
                licensing = it.licensing,
                editing = it.editing,
                marketing = it.marketing,
                companies = it.companies,
                payout = it.payout,
                other = it.other
            )
        },
        costs = costs.let {
            ContentPartnerContractCostsRequest(
                minimumGuarantee = it.minimumGuarantee,
                upfrontLicense = it.upfrontLicense,
                technicalFee = it.technicalFee,
                recoupable = it.recoupable
            )
        }
    )
}

object ContractRestrictionsFactory {
    fun sample(
        clientFacing: List<String> = listOf("restriction one"),
        territory: String = "territory",
        licensing: String = "licensing",
        editing: String = "editing",
        marketing: String = "marketing",
        companies: String = "companies",
        payout: String = "payout",
        other: String = "other"
    ) = ContractRestrictions(
        clientFacing = clientFacing,
        territory = territory,
        licensing = licensing,
        editing = editing,
        marketing = marketing,
        companies = companies,
        payout = payout,
        other = other
    )
}

object ContractCostsFactory {
    fun sample(
        minimumGuarantee: List<BigDecimal> = listOf(BigDecimal.ONE),
        upfrontLicense: BigDecimal = BigDecimal.ONE,
        technicalFee: BigDecimal = BigDecimal.ONE,
        recoupable: Boolean = true
    ) = ContractCosts(
        minimumGuarantee = minimumGuarantee,
        upfrontLicense = upfrontLicense,
        technicalFee = technicalFee,
        recoupable = recoupable
    )
}
