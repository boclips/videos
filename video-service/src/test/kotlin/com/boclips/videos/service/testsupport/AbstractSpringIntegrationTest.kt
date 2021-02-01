package com.boclips.videos.service.testsupport

import com.boclips.contentpartner.service.application.agerange.CreateAgeRange
import com.boclips.contentpartner.service.application.channel.CreateChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.application.exceptions.ChannelConflictException
import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.kalturaclient.clients.TestKalturaClient
import com.boclips.kalturaclient.flavorAsset.Asset
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.infrastructure.contract.ChannelIndexFake
import com.boclips.search.service.infrastructure.contract.CollectionIndexFake
import com.boclips.search.service.infrastructure.contract.VideoIndexFake
import com.boclips.search.service.infrastructure.contract.SubjectIndexFake
import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.httpclient.test.fakes.ContentPackagesClientFake
import com.boclips.users.api.httpclient.test.fakes.OrganisationsClientFake
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.VideoServiceApiFactory.Companion.createCollectionRequest
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.api.request.contentwarning.CreateContentWarningRequest
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.request.tag.CreateTagRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.contentwarning.CreateContentWarning
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.tag.CreateTag
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.application.video.indexing.VideoIndexUpdater
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VoiceType
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.collection.CollectionSubjects
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.video.MongoVideoRepository
import com.boclips.videos.service.testsupport.ContentPackageResourceFactory.createContentPackageResource
import com.damnhandy.uri.template.UriTemplate
import com.jayway.jsonpath.JsonPath
import com.mongodb.MongoClient
import com.nhaarman.mockitokotlin2.reset
import de.flapdoodle.embed.mongo.MongodProcess
import mu.KLogging
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Collections
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles(
    "test",
    "fakes",
    "fakes-kaltura",
    "fakes-search",
    "fakes-youtube",
    "fakes-security",
    "fakes-signed-link",
    "fake-user-service"
)
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var legacyVideoSearchService: LegacyVideoSearchService

    @Autowired
    lateinit var videoIndexFake: VideoIndexFake

    @Autowired
    lateinit var collectionIndexFake: CollectionIndexFake

    @Autowired
    lateinit var channelIndexFake: ChannelIndexFake

    @Autowired
    lateinit var subjectsIndexFake: SubjectIndexFake

    @Autowired
    lateinit var fakeKalturaClient: TestKalturaClient

    @Autowired
    lateinit var fakeYoutubePlaybackProvider: TestYoutubePlaybackProvider

    @Autowired
    lateinit var kalturaPlaybackProvider: KalturaPlaybackProvider

    @Autowired
    lateinit var createVideo: CreateVideo

    @Autowired
    lateinit var updateVideo: UpdateVideo

    @Autowired
    lateinit var createCollection: CreateCollection

    @Autowired
    lateinit var createChannel: CreateChannel

    @Autowired
    lateinit var getChannels: GetChannels

    @Autowired
    lateinit var updateCollection: UpdateCollection

    @Autowired
    lateinit var bookmarkCollection: BookmarkCollection

    @Autowired
    lateinit var mongoClient: MongoClient

    @Autowired
    lateinit var fakeEventBus: SynchronousFakeEventBus

    @Autowired
    lateinit var createSubject: CreateSubject

    @Autowired
    lateinit var createTag: CreateTag

    @Autowired
    lateinit var createAgeRange: CreateAgeRange

    @Autowired
    lateinit var videoIndexUpdater: VideoIndexUpdater

    @Autowired
    lateinit var subjectClassificationService: SubjectClassificationService

    @Autowired
    lateinit var accessRuleService: AccessRuleService

    @Autowired
    lateinit var usersClient: UsersClientFake

    @Autowired
    lateinit var contentPackagesClient: ContentPackagesClientFake

    @Autowired
    lateinit var organisationsClient: OrganisationsClientFake

    @Autowired
    lateinit var createLegalRestrictions: CreateLegalRestrictions

    @Autowired
    lateinit var collectionSubjects: CollectionSubjects

    @Autowired
    lateinit var createContentWarning: CreateContentWarning

    @Autowired
    lateinit var cacheManager: CacheManager

    @LocalServerPort
    var randomServerPort: Int = 0

    companion object : KLogging() {
        private var mongoProcess: MongodProcess? = null

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            if (mongoProcess == null) {
                mongoProcess = TestMongoProcess.process
            }
        }
    }

    @BeforeEach
    fun resetState() {
        mongoClient.apply {
            listDatabaseNames()
                .filterNot { setOf("admin", "config").contains(it) }
                .forEach {
                    println("Dropping $it")
                    dropDatabase(it)
                }
        }

        collectionIndexFake.safeRebuildIndex(emptySequence())
        videoIndexFake.safeRebuildIndex(emptySequence())
        channelIndexFake.safeRebuildIndex(emptySequence())
        subjectsIndexFake.safeRebuildIndex(emptySequence())

        fakeYoutubePlaybackProvider.clear()
        fakeKalturaClient.clear()

        fakeEventBus.clearState()

        usersClient.clear()
        contentPackagesClient.clear()
        organisationsClient.clear()

        reset(legacyVideoSearchService)

        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
    }

    fun createMediaEntry(
        id: String = "1",
        duration: Duration = Duration.ofMinutes(1),
        status: MediaEntryStatus = MediaEntryStatus.READY,
        width: Int = 1920,
        height: Int = 1080,
        assets: Set<Asset> = emptySet()
    ) {
        val mediaEntry = MediaEntry.builder()
            .referenceId("ref-$id")
            .id(id)
            .downloadUrl("https://download.com/entryId/$id/format/download")
            .duration(duration)
            .status(status)
            .playCount(0)
            .tags(Collections.emptyList())
            .flavorParamsIds(listOf("1", "2", "3", "4"))
            .createdAt(ZonedDateTime.now(ZoneOffset.UTC))
            .conversionProfileId(1234560)
            .width(width)
            .height(height)
            .build()
        fakeKalturaClient.addMediaEntry(mediaEntry)
        if (assets.isNotEmpty()) {
            fakeKalturaClient.setAssets(id, assets.toMutableList())
        }
    }

    fun saveVideo(
        playbackId: PlaybackId = PlaybackId(
            type = KALTURA,
            value = "id-${UUID.randomUUID()}"
        ),
        title: String = "Some title!",
        description: String = "Some description!",
        additionalDescription: String? = "additional description",
        date: String = "2018-01-01",
        duration: Duration = Duration.ofSeconds(120),
        contentProvider: String = "Reuters",
        contentProviderId: String? = null,
        contentProviderVideoId: String = "content-partner-video-id-${playbackId.value}",
        keywords: List<String> = emptyList(),
        types: List<VideoType> = listOf(VideoType.INSTRUCTIONAL_CLIPS),
        legalRestrictions: String = "",
        ageRangeMin: Int? = 7,
        ageRangeMax: Int? = 11,
        distributionMethods: Set<DistributionMethodResource> = setOf(
            DistributionMethodResource.DOWNLOAD,
            DistributionMethodResource.STREAM
        ),
        subjectIds: Set<String> = setOf(),
        language: String? = null,
        width: Int = 1920,
        height: Int = 1080,
        assets: Set<Asset> = setOf(KalturaFactories.createKalturaAsset(height = 1080)),
        isVoiced: Boolean? = null
    ): VideoId {
        val retrievedContentPartnerId =
            saveChannel(name = contentProvider, distributionMethods = distributionMethods).id.value

        when (playbackId.type) {
            KALTURA -> createMediaEntry(
                id = playbackId.value,
                duration = duration,
                assets = assets,
                width = width,
                height = height
            )
            YOUTUBE -> {
                fakeYoutubePlaybackProvider.addVideo(
                    youtubeId = playbackId.value,
                    thumbnailUrl = "https://youtube.com/thumb/${playbackId.value}.png",
                    duration = duration
                )

                fakeYoutubePlaybackProvider.addMetadata(playbackId.value, "Another amazing YT Channel", "channel-1")
            }
        }

        val video = createVideo(
            CreateVideoRequest(
                providerId = contentProviderId ?: retrievedContentPartnerId,
                providerVideoId = contentProviderVideoId,
                title = title,
                description = description,
                additionalDescription = additionalDescription,
                releasedOn = LocalDate.parse(date),
                legalRestrictions = legalRestrictions,
                keywords = keywords,
                videoTypes = types.map { it.name },
                playbackId = playbackId.value,
                playbackProvider = playbackId.type.name,
                analyseVideo = false,
                ageRangeMin = ageRangeMin,
                ageRangeMax = ageRangeMax,
                subjects = subjectIds,
                language = language,
                isVoiced = isVoiced
            ),
            UserFactory.sample()
        )

        fakeEventBus.clearState()

        return video.videoId
    }

    fun saveSubject(name: String): Subject {
        return createSubject(CreateSubjectRequest(name))
    }

    fun addVideoAttachment(attachment: AttachmentRequest, videoId: VideoId) {
        updateVideo(
            videoId.value,
            UpdateVideoRequest(attachments = Specified(listOf(attachment))),
            user = UserFactory.sample(boclipsEmployee = true)
        )
    }

    fun saveAgeRange(
        id: String,
        min: Int,
        max: Int,
        label: String
    ): com.boclips.contentpartner.service.domain.model.agerange.AgeRange {
        return createAgeRange(
            AgeRangeRequest(
                id = id,
                min = min,
                max = max,
                label = label
            )
        )
    }

    fun saveTag(label: String): String {
        return createTag(CreateTagRequest(label = label)).id
    }

    fun setVideoSubjects(videoId: String, vararg subjectIds: SubjectId) {
        subjectClassificationService.videoClassified(
            VideoSubjectClassified.builder()
                .videoId(videoId)
                .subjects(subjectIds.map { com.boclips.eventbus.domain.SubjectId(it.value) }.toSet())
                .build()
        )
    }

    data class SaveCollectionRequest(
        val owner: String = "owner@me.com",
        val title: String = "collection title",
        val videos: List<String> = emptyList(),
        val public: Boolean = false,
        val bookmarkedBy: String? = null,
        val subjects: Set<Subject> = emptySet()
    )

    fun saveCollection(request: SaveCollectionRequest): CollectionId {
        return saveCollection(
            owner = request.owner,
            title = request.title,
            videos = request.videos,
            discoverable = request.public,
            bookmarkedBy = request.bookmarkedBy,
            subjects = request.subjects
        )
    }

    fun saveCollection(
        owner: String = "owner@me.com",
        title: String = "collection title",
        videos: List<String> = emptyList(),
        discoverable: Boolean = false,
        bookmarkedBy: String? = null,
        subjects: Set<Subject> = emptySet(),
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        attachment: AttachmentRequest? = null
    ): CollectionId {
        val user = UserFactory.sample(id = owner)

        val collectionId = createCollection(
            createCollectionRequest = createCollectionRequest(title = title, videos = videos),
            requester = user
        ).id

        updateCollection(
            collectionId = collectionId.value,
            updateCollectionRequest = UpdateCollectionRequest(
                discoverable = discoverable,
                subjects = subjects.map { it.id.value }.toSet(),
                ageRange = com.boclips.videos.api.request.agerange.AgeRangeRequest(ageRangeMin, ageRangeMax),
                attachment = attachment
            ),
            requester = user
        )

        bookmarkedBy?.let {
            bookmarkCollection(collectionId = collectionId.value, user = UserFactory.sample(id = it))
        }

        fakeEventBus.clearState()

        return collectionId
    }

    fun saveContentWarning(label: String): ContentWarning = createContentWarning(CreateContentWarningRequest(label))

    fun saveChannel(
        name: String = "TeD",
        ageRanges: List<String>? = emptyList(),
        distributionMethods: Set<DistributionMethodResource>? = null,
        currency: String? = null,
        contentTypes: List<String>? = emptyList()
    ): Channel {
        val createdChannel = try {
            createChannel(
                VideoServiceApiFactory.createChannelRequest(
                    name = name,
                    ageRanges = ageRanges,
                    distributionMethods = distributionMethods,
                    currency = currency,
                    contentTypes = contentTypes
                )
            )
        } catch (e: ChannelConflictException) {
            getChannel(name)
        }

        fakeEventBus.clearState()

        return createdChannel
    }

    fun getChannel(name: String): Channel {
        return getChannels.invoke(name = name).first()
    }

    fun saveLegalRestrictions(text: String = "No restrictions."): LegalRestrictionsId {
        val createdResource = createLegalRestrictions(text = text)
        return LegalRestrictionsId(
            createdResource.id.value
        )
    }

    fun ResultActions.andExpectApiErrorPayload(): ResultActions {
        return this.andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.path").exists())
    }

    fun ResultActions.andReturnLink(linkName: String): UriTemplate {
        val hrefPath = "$._links.$linkName.href"
        andExpect(jsonPath(hrefPath).exists())

        val response = andReturn().response.contentAsString
        val link = JsonPath.parse(response).read<String>(hrefPath)
        return UriTemplate.fromTemplate(link)
    }

    fun addAccessToVideoIds(userId: String, vararg contractedVideoIds: String) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedVideos(
                    id = "access-rule-id",
                    name = UUID.randomUUID().toString(),
                    videoIds = contractedVideoIds.toList()
                )
            )
        )
    }

    fun saveContentPackage(
        id: String,
        name: String,
        vararg accessRules: AccessRuleResource
    ) =
        contentPackagesClient.add(
            createContentPackageResource(id, name, accessRules.toList())
        )

    fun addDistributionMethodAccessRule(
        userId: String,
        vararg includedDistributionMethods: DistributionMethodResource
    ) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedDistributionMethods(
                    id = "access-rule-id",
                    name = UUID.randomUUID().toString(),
                    distributionMethods = includedDistributionMethods.map { it.name }
                )
            )
        )
    }

    fun removeAccessToVideo(userId: String, vararg excludedVideoIds: String) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(
                AccessRuleResource.ExcludedVideos(
                    id = "access-rule-id",
                    name = UUID.randomUUID().toString(),
                    videoIds = excludedVideoIds.toList()
                )
            )
        )
    }

    fun removeAccessToVideoTypes(userId: String, vararg excludedVideoType: VideoType) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(
                AccessRuleResource.ExcludedVideoTypes(
                    id = "access-rule-id",
                    name = UUID.randomUUID().toString(),
                    videoTypes = excludedVideoType.map { it.name }
                )
            )
        )
    }

    fun addAccessToVideoTypes(userId: String, vararg includedVideoType: VideoType) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedVideoTypes(
                    id = "access-rule-id",
                    name = UUID.randomUUID().toString(),
                    videoTypes = includedVideoType.map { it.name }
                )
            )
        )
    }

    fun addAccessToVoiceType(userId: String, vararg voiceType: VoiceType) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedVideoVoiceTypes(
                    id = "access-rule-id",
                    name = UUID.randomUUID().toString(),
                    voiceTypes = voiceType.map { it.name }
                )
            )
        )
    }

    fun removeAccessToChannel(userId: String, vararg excludeContentPartners: String) {
        usersClient.addAccessRules(
            userId,
            AccessRulesResourceFactory.sample(
                AccessRuleResource.ExcludedChannels(
                    id = "access-rule-id",
                    name = UUID.randomUUID().toString(),
                    channelIds = excludeContentPartners.toList()
                )
            )
        )
    }

    fun mongoVideosCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection(MongoVideoRepository.collectionName)

    fun userAssignedToOrganisation(id: String = "the@teacher.com", customPrices: DealResource.PricesResource? = null): User {
        val organisation = organisationsClient.add(OrganisationResourceFactory.sample(
            deal = DealResource(
                prices = customPrices,
                accessExpiresOn = null,
                billing = null,
                contentPackageId = null
            )
        ))

        val userResource = usersClient.add(
            UserResourceFactory.sample(
                id = id,
                organisation = organisation.organisationDetails
            )
        )
        return UserFactory.sample(
            id = userResource.id
        )
    }
}
