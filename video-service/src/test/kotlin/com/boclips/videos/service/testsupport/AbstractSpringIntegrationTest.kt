package com.boclips.videos.service.testsupport

import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.contentPartner.CreateContentPartner
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.tag.CreateTag
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.BulkVideoSearchUpdate
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.BoundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.mongodb.MongoClient
import com.nhaarman.mockito_kotlin.reset
import de.flapdoodle.embed.mongo.MongodProcess
import mu.KLogging
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
    lateinit var bulkVideoSearchUpdate: BulkVideoSearchUpdate

    @Autowired
    lateinit var subjectClassificationService: SubjectClassificationService

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

        reset(legacyVideoSearchService)
    }

    fun saveVideo(
        playbackId: PlaybackId = PlaybackId(type = KALTURA, value = "ref-id-${UUID.randomUUID()}"),
        title: String = "Some title!",
        description: String = "Some description!",
        date: String = "2018-01-01",
        duration: Duration = Duration.ofSeconds(120),
        contentProvider: String = "Reuters",
        contentProviderId: String? = null,
        contentProviderVideoId: String = "content-partner-video-id-${playbackId.value}",
        legacyType: LegacyVideoType = LegacyVideoType.INSTRUCTIONAL_CLIPS,
        keywords: List<String> = emptyList(),
        legalRestrictions: String = "",
        ageRange: AgeRange = BoundedAgeRange(min = 7, max = 11)
    ): VideoId {
        createContentPartner(
            ContentPartnerRequest(
                name = contentProvider,
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD, DistributionMethodResource.STREAM)
            )
        )

        when (playbackId.type) {
            KALTURA -> fakeKalturaClient.addMediaEntry(
                createMediaEntry(
                    id = "entry-${playbackId.value}",
                    referenceId = playbackId.value,
                    duration = duration
                )
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
                provider = contentProvider,
                providerId = contentProviderId,
                providerVideoId = contentProviderVideoId,
                title = title,
                description = description,
                releasedOn = LocalDate.parse(date),
                legalRestrictions = legalRestrictions,
                keywords = keywords,
                videoType = legacyType.name,
                playbackId = playbackId.value,
                playbackProvider = playbackId.type.name,
                analyseVideo = false,
                ageRangeMin = ageRange.min(),
                ageRangeMax = ageRange.max()
            )
        )

        val id = video.content.id

        fakeEventBus.clearState()

        return VideoId(id!!)
    }

    fun saveSubject(name: String): SubjectId {
        val subjectId = createSubject(CreateSubjectRequest(name)).id
        return SubjectId(subjectId)
    }

    fun setVideoSubjects(videoId: String, vararg subjectIds: SubjectId) {
        subjectClassificationService.videoClassified(
            VideoSubjectClassified.builder()
                .videoId(videoId)
                .subjects(subjectIds.map { com.boclips.eventbus.domain.SubjectId(it.value) }.toSet())
                .build()
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
        distributionMethods: Set<DistributionMethodResource>? = null
    ): ContentPartner {
        val createdContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = name,
                ageRange = ageRange,
                accreditedToYtChannel = accreditedToYtChannel,
                distributionMethods = distributionMethods
            )
        )

        fakeEventBus.clearState()

        return createdContentPartner
    }

    fun ResultActions.andExpectApiErrorPayload(): ResultActions {
        return this.andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.path").exists())
    }
}
