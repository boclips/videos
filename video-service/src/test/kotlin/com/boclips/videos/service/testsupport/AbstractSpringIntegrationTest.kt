package com.boclips.videos.service.testsupport

import com.boclips.contentpartner.service.application.agerange.CreateAgeRange
import com.boclips.contentpartner.service.application.channel.CreateChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
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
import com.boclips.search.service.infrastructure.contract.VideoSearchServiceFake
import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.httpclient.test.fakes.OrganisationsClientFake
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.VideoServiceApiFactory.Companion.createCollectionRequest
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.api.request.channel.AgeRangeRequest
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
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.collection.CollectionSubjects
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.video.MongoVideoRepository
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
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
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
    lateinit var videoSearchService: VideoSearchServiceFake

    @Autowired
    lateinit var collectionIndex: CollectionIndex

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
    lateinit var indexUpdater: VideoIndexUpdater

    @Autowired
    lateinit var subjectClassificationService: SubjectClassificationService

    @Autowired
    lateinit var accessRuleService: AccessRuleService

    @Autowired
    lateinit var usersClient: UsersClientFake

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

        collectionIndex.safeRebuildIndex(emptySequence())
        videoSearchService.safeRebuildIndex(emptySequence())

        fakeYoutubePlaybackProvider.clear()
        fakeKalturaClient.clear()

        fakeEventBus.clearState()

        usersClient.clear()
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
        date: String = "2018-01-01",
        duration: Duration = Duration.ofSeconds(120),
        contentProvider: String = "Reuters",
        contentProviderId: String? = null,
        contentProviderVideoId: String = "content-partner-video-id-${playbackId.value}",
        type: ContentType = ContentType.INSTRUCTIONAL_CLIPS,
        keywords: List<String> = emptyList(),
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
        assets: Set<Asset> = setOf(KalturaFactories.createKalturaAsset(height = 1080))
    ): VideoId {
        val retrievedContentPartnerId =
            saveContentPartner(name = contentProvider, distributionMethods = distributionMethods).id.value

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
                releasedOn = LocalDate.parse(date),
                legalRestrictions = legalRestrictions,
                keywords = keywords,
                videoType = type.name,
                playbackId = playbackId.value,
                playbackProvider = playbackId.type.name,
                analyseVideo = false,
                ageRangeMin = ageRangeMin,
                ageRangeMax = ageRangeMax,
                subjects = subjectIds,
                language = language
            )
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

    fun saveContentPartner(
        name: String = "TeD",
        ageRanges: List<String>? = emptyList(),
        accreditedToYtChannel: String? = null,
        distributionMethods: Set<DistributionMethodResource>? = null,
        currency: String? = null
    ): Channel {
        val createdContentPartner = try {
            createChannel(
                VideoServiceApiFactory.createChannelRequest(
                    name = name,
                    ageRanges = ageRanges,
                    accreditedToYtChannel = accreditedToYtChannel,
                    distributionMethods = distributionMethods,
                    currency = currency
                )
            )
        } catch (e: ContentPartnerConflictException) {
            getChannels.invoke(name = name).first()
        }

        fakeEventBus.clearState()

        return createdContentPartner
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
            userId, AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedVideos(
                    name = UUID.randomUUID().toString(),
                    videoIds = contractedVideoIds.toList()
                )
            )
        )
    }

    fun addsAccessToStreamingVideos(userId: String, vararg includedDistributionMethods: DistributionMethodResource) {
        usersClient.addAccessRules(
            userId, AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedDistributionMethod(name = UUID.randomUUID().toString(),
                    distributionMethods = includedDistributionMethods.map { it.name })
            )
        )
    }

    fun removeAccessToVideo(userId: String, vararg excludedVideoIds: String) {
        usersClient.addAccessRules(
            userId, AccessRulesResourceFactory.sample(
                AccessRuleResource.ExcludedVideos(
                    name = UUID.randomUUID().toString(),
                    videoIds = excludedVideoIds.toList()
                )
            )
        )
    }

    fun addAccessToVideoTypes(userId: String, vararg excludedVideoType: ContentType) {
        usersClient.addAccessRules(
            userId, AccessRulesResourceFactory.sample(
                AccessRuleResource.ExcludedVideoTypes(name = UUID.randomUUID().toString(),
                    videoTypes = excludedVideoType.map { it.name })
            )
        )
    }

    fun removeAccessToContentPartner(userId: String, vararg excludeContentPartners: String) {
        usersClient.addAccessRules(
            userId, AccessRulesResourceFactory.sample(
                AccessRuleResource.ExcludedContentPartners(
                    name = UUID.randomUUID().toString(),
                    contentPartnerIds = excludeContentPartners.toList()
                )
            )
        )
    }

    fun mongoVideosCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection(MongoVideoRepository.collectionName)
}
