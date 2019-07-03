package com.boclips.videos.service.testsupport

import com.boclips.events.config.Subscriptions
import com.boclips.events.config.Topics
import com.boclips.events.types.Subject
import com.boclips.events.types.video.VideoSubjectClassified
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.BookmarkCollection
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.collection.UpdateCollection
import com.boclips.videos.service.application.contentPartner.CreateContentPartner
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.UpdateVideoSubjects
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.BoundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResourceDeliveryMethod
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.messaging.MessageChannel
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
    lateinit var topics: Topics

    @Autowired
    lateinit var subscriptions: Subscriptions

    @Autowired
    lateinit var messageCollector: MessageCollector

    @Autowired
    lateinit var messageChannels: List<MessageChannel>

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var createSubject: CreateSubject

    @Autowired
    lateinit var updateVideoSubjects: UpdateVideoSubjects

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

        messageChannels.forEach { channel ->
            try {
                messageCollector.forChannel(channel).clear()
            } catch (e: Exception) {
            }
        }

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
        searchable: Boolean = true,
        legalRestrictions: String = "",
        ageRange: AgeRange = BoundedAgeRange(min = 7, max = 11),
        hiddenFromSearchForDeliveryMethods: Set<VideoResourceDeliveryMethod> = emptySet()
    ): VideoId {
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

        val id = createVideo(
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
                searchable = searchable,
                hiddenFromSearchForDeliveryMethods = hiddenFromSearchForDeliveryMethods,
                analyseVideo = false,
                ageRangeMin = ageRange.min(),
                ageRangeMax = ageRange.max()
            )
        ).content.id

        return VideoId(id!!)
    }

    fun saveSubject(name: String): SubjectId {
        val subjectId = createSubject(CreateSubjectRequest(name)).id
        return SubjectId(subjectId)
    }

    fun setVideoSubjects(videoId: String, vararg subjectIds: SubjectId) {
        updateVideoSubjects(
            VideoSubjectClassified.builder()
                .videoId(videoId)
                .subjects(subjectIds.map { Subject.builder().id(it.value).build() }.toSet())
                .build()
        )
    }

    fun saveCollection(
        owner: String = "owner@me.com",
        title: String = "collection title",
        videos: List<String> = emptyList(),
        public: Boolean = false,
        bookmarkedBy: String? = null
    ): CollectionId {
        setSecurityContext(owner)

        val collectionId = createCollection(TestFactories.createCollectionRequest(title = title, videos = videos)).id

        updateCollection(collectionId.value, UpdateCollectionRequest(isPublic = public))
        messageCollector.forChannel(topics.collectionVisibilityChanged()).clear()

        bookmarkedBy?.let {
            setSecurityContext(it)
            bookmarkCollection(collectionId.value)
            messageCollector.forChannel(topics.collectionBookmarkChanged()).clear()
        }

        return collectionId
    }

    fun saveContentPartner(
        name: String = "TeD",
        ageRange: AgeRangeRequest = AgeRangeRequest(3, 10),
        accreditedToYtChannel: String? = null,
        searchable: Boolean = true
    ): ContentPartner {
        return createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = name,
                ageRange = ageRange,
                accreditedToYtChannel = accreditedToYtChannel,
                searchable = searchable
            )
        )
    }

    fun changeVideoStatus(id: String, status: VideoResourceStatus) {
        bulkUpdateVideo(BulkUpdateRequest(listOf(id), status))
    }

    fun ResultActions.andExpectApiErrorPayload(): ResultActions {
        return this.andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.path").exists())
    }
}
