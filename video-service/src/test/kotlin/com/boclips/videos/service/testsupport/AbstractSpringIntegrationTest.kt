package com.boclips.videos.service.testsupport

import com.boclips.contentpartner.service.application.ContentPartnerConflictException
import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.CreateLegalRestrictions
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.security.testing.setSecurityContext
import com.boclips.users.client.implementation.FakeUserServiceClient
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.tag.CreateTag
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.VideoSearchUpdater
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.BoundedAgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.infrastructure.collection.CollectionSubjects
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequest
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
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.Duration
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test", "fakes", "fakes-kaltura", "fakes-search", "fakes-youtube", "fakes-security")
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var legacyVideoSearchService: LegacyVideoSearchService

    @Autowired
    lateinit var videoSearchService: VideoSearchService

    @Autowired
    lateinit var collectionSearchService: CollectionSearchService

    @Autowired
    lateinit var fakeKalturaClient: TestKalturaClient

    @Autowired
    lateinit var fakeYoutubePlaybackProvider: TestYoutubePlaybackProvider

    @Autowired
    lateinit var kalturaPlaybackProvider: KalturaPlaybackProvider

    @Autowired
    lateinit var createVideo: CreateVideo

    @Autowired
    lateinit var createCollection: CreateCollection

    @Autowired
    lateinit var createContentPartner: CreateContentPartner

    @Autowired
    lateinit var getContentPartners: GetContentPartners

    @Autowired
    lateinit var updateCollection: UpdateCollection

    @Autowired
    lateinit var bookmarkCollection: BookmarkCollection

    @Autowired
    lateinit var bulkUpdateVideo: BulkUpdateVideo

    @Autowired
    lateinit var mongoClient: MongoClient

    @Autowired
    lateinit var fakeEventBus: SynchronousFakeEventBus

    @Autowired
    lateinit var createSubject: CreateSubject

    @Autowired
    lateinit var createTag: CreateTag

    @Autowired
    lateinit var searchUpdater: VideoSearchUpdater

    @Autowired
    lateinit var subjectClassificationService: SubjectClassificationService

    @Autowired
    lateinit var accessRuleService: AccessRuleService

    @Autowired
    lateinit var userServiceClient: FakeUserServiceClient

    @Autowired
    lateinit var createLegalRestrictions: CreateLegalRestrictions

    @Autowired
    lateinit var collectionSubjects: CollectionSubjects

    @Autowired
    lateinit var cacheManager: CacheManager

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

        collectionSearchService.safeRebuildIndex(emptySequence())
        videoSearchService.safeRebuildIndex(emptySequence())

        fakeYoutubePlaybackProvider.clear()
        fakeKalturaClient.clear()

        fakeEventBus.clearState()

        userServiceClient.clearContracts()
        userServiceClient.clearUser()

        reset(legacyVideoSearchService)

        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
    }

    fun createMediaEntry(
        id: String = "1",
        duration: Duration = Duration.ofMinutes(1),
        status: MediaEntryStatus = MediaEntryStatus.READY
    ) =
        fakeKalturaClient.createMediaEntry(id, "ref-$id", duration, status)

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
        contentType: ContentType = ContentType.INSTRUCTIONAL_CLIPS,
        keywords: List<String> = emptyList(),
        legalRestrictions: String = "",
        ageRange: AgeRange = BoundedAgeRange(min = 7, max = 11),
        distributionMethods: Set<DistributionMethodResource> = setOf(
            DistributionMethodResource.DOWNLOAD,
            DistributionMethodResource.STREAM
        ),
        subjectIds: Set<String> = setOf()
    ): VideoId {
        val user = getCurrentUser()
        val retrievedContentPartnerId = try {
            createContentPartner(
                ContentPartnerRequest(
                    name = contentProvider,
                    distributionMethods = distributionMethods
                )
            ).contentPartnerId.value
        } catch (e: ContentPartnerConflictException) {
            getContentPartners.invoke(user = user, name = contentProvider).firstOrNull()?.content?.id
        }

        when (playbackId.type) {
            KALTURA -> createMediaEntry(
                id = playbackId.value,
                duration = duration
            )
            YOUTUBE -> {
                fakeYoutubePlaybackProvider.addVideo(
                    playbackId.value,
                    "https://youtube.com/thumb/${playbackId.value}.png",
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
                videoType = contentType.name,
                playbackId = playbackId.value,
                playbackProvider = playbackId.type.name,
                analyseVideo = false,
                ageRangeMin = ageRange.min(),
                ageRangeMax = ageRange.max(),
                subjects = subjectIds
            )
        )

        fakeEventBus.clearState()

        return video.videoId
    }

    fun saveSubject(name: String): Subject {
        return createSubject(CreateSubjectRequest(name))
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
            public = request.public,
            bookmarkedBy = request.bookmarkedBy,
            subjects = request.subjects
        )
    }

    fun saveCollection(
        owner: String = "owner@me.com",
        title: String = "collection title",
        videos: List<String> = emptyList(),
        public: Boolean = false,
        bookmarkedBy: String? = null,
        subjects: Set<Subject> = emptySet()
    ): CollectionId {
        setSecurityContext(owner)

        val collectionId = createCollection(TestFactories.createCollectionRequest(title = title, videos = videos)).id

        updateCollection(
            collectionId.value,
            UpdateCollectionRequest(isPublic = public, subjects = subjects.map { it.id.value }.toSet())
        )

        bookmarkedBy?.let {
            setSecurityContext(it)
            bookmarkCollection(collectionId.value)
        }

        fakeEventBus.clearState()

        return collectionId
    }

    fun saveContentPartner(
        name: String = "TeD",
        ageRange: AgeRangeRequest = AgeRangeRequest(3, 10),
        accreditedToYtChannel: String? = null,
        distributionMethods: Set<DistributionMethodResource>? = null,
        currency: String? = null
    ): ContentPartner {
        val createdContentPartner = createContentPartner(
            com.boclips.contentpartner.service.testsupport.TestFactories.createContentPartnerRequest(
                name = name,
                ageRange = ageRange,
                accreditedToYtChannel = accreditedToYtChannel,
                distributionMethods = distributionMethods,
                currency = currency
            )
        )

        fakeEventBus.clearState()

        return createdContentPartner
    }

    fun saveLegalRestrictions(text: String = "No restrictions."): LegalRestrictionsId {
        val createdResource = createLegalRestrictions(text = text)
        return LegalRestrictionsId(createdResource.id)
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
}
