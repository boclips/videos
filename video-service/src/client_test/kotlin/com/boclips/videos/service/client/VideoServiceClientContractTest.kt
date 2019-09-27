package com.boclips.videos.service.client

import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.security.testing.setSecurityContext
import com.boclips.users.client.model.contract.SelectedContentContract
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException
import com.boclips.videos.service.client.exceptions.VideoExistsException
import com.boclips.videos.service.client.internal.FakeClient
import com.boclips.videos.service.client.testsupport.AbstractVideoServiceClientSpringIntegrationTest
import com.boclips.videos.service.client.testsupport.TestFactories
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal abstract class VideoServiceClientContractTest : AbstractVideoServiceClientSpringIntegrationTest() {

    abstract fun getClient(): VideoServiceClient

    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Test
    fun `get a video by entry id`() {
        val client = getClient()

        val subject = client.subjects.first { it.name == "Maths" }

        val contentPartnerId =
            client.createContentPartner(TestFactories.createContentPartnerRequest(name = "test-content-partner"))

        val playbackId = "entry-123"
        val id = client.createVideo(
            TestFactories.createCreateVideoRequest(
                title = "the title",
                description = "the description",
                playbackId = playbackId,
                playbackProvider = PlaybackProvider.KALTURA,
                contentProviderId = contentPartnerId.value,
                subjects = setOf(subject.id.value)
            )
        )

        val video = client.get(id)

        assertThat(video.title).isEqualTo("the title")
        assertThat(video.description).isEqualTo("the description")
        assertThat(video.releasedOn).isCloseTo(LocalDate.now(), within(1, ChronoUnit.DAYS))
        assertThat(video.createdBy).isEqualTo("test-content-partner")
        assertThat(video.contentPartnerId).isEqualTo(contentPartnerId.value)
        assertThat(video.playback?.playbackId).isEqualTo("entry-123")
        assertThat(video.playback?.referenceId).isEqualTo("ref-entry-123")
        assertThat(video.playback?.thumbnailUrl).isNotBlank()
        assertThat(video.playback?.duration).isNotNull()

        assertThat(video.subjects).hasSize(1)
        assertThat(video.subjects.first().id.value).isEqualTo(subject.id.value)
        assertThat(video.subjects.first().name).isEqualTo(subject.name)
    }

    @Test
    fun `404 error is thrown when requested video is not found`() {
        val contentPartnerId =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest())

        val realVideoUriString = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                title = "the title",
                description = "the description",
                playbackId = "entry-123",
                contentProviderId = contentPartnerId.value
            )
        ).uri.toString()
        val invalidId = VideoId(URI("${realVideoUriString.substringBeforeLast("/")}/000000000000000000000000"))

        assertThatThrownBy { getClient().get(invalidId) }
            .isInstanceOf(HttpClientErrorException::class.java)
            .extracting("statusCode")
            .containsOnly(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `get VideoId for raw identifier`() {
        val contentPartnerId =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest())

        val rawId =
            getClient().createVideo(
                TestFactories.createCreateVideoRequest(
                    playbackId = "entry-123",
                    contentProviderId = contentPartnerId.value
                )
            ).uri.toString()
                .split('/').last()

        val id = getClient().rawIdToVideoId(rawId)

        assertThat(id.uri.toString()).matches("https?://.*/videos/$rawId")
    }

    @Test
    fun `get official content partners`() {
        val id = getClient().createContentPartner(
            CreateContentPartnerRequest
                .builder()
                .name("ted ed")
                .accreditedToYtChannelId(null)
                .build()
        )

        val contentPartners = getClient().findOfficialContentPartner("ted ed")

        assertThat(contentPartners).hasSize(1)
        assertThat(contentPartners[0].name).isEqualTo("ted ed")
        assertThat(contentPartners[0].contentPartnerId).isEqualTo(id)
        assertThat(contentPartners[0].official).isTrue()
    }

    @Test
    fun `get official content partner by id`() {
        val id = getClient().createContentPartner(
            CreateContentPartnerRequest
                .builder()
                .name("ted ed")
                .accreditedToYtChannelId(null)
                .build()
        )

        val contentPartners = getClient().findContentPartner(id)

        assertThat(contentPartners.name).isEqualTo("ted ed")
        assertThat(contentPartners.contentPartnerId).isEqualTo(id)
        assertThat(contentPartners.official).isTrue()
    }

    @Test
    fun `get youtube content partners`() {
        val id = getClient().createContentPartner(
            CreateContentPartnerRequest
                .builder()
                .name("ted")
                .accreditedToYtChannelId("123")
                .build()
        )

        val contentPartners = getClient().findContentPartnerByYoutubeChannelId("123")

        assertThat(contentPartners).hasSize(1)
        assertThat(contentPartners[0].name).isEqualTo("ted")
        assertThat(contentPartners[0].contentPartnerId).isEqualTo(id)
        assertThat(contentPartners[0].official).isFalse()
    }

    @Test
    fun `get CollectionId for raw identifier`() {
        val rawId = "test-collection-identifier"

        val id = getClient().rawIdToCollectionId(rawId)

        assertThat(id.uri.toString()).matches("https?://.*/collections/$rawId")
    }

    @Test
    fun `404 error is thrown when requested collection is not found`() {
        val invalidId = getClient().rawIdToCollectionId("000000000000000000000000")

        assertThatThrownBy { getClient().get(invalidId) }
            .isInstanceOf(HttpClientErrorException::class.java)
            .extracting("statusCode")
            .containsOnly(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `get a Collection returns a collection with shallow video details`() {
        val testCollection = getClient().myCollections.component1()

        val collection = getClient().get(testCollection.collectionId)

        assertThat(collection.title).isEqualTo(testCollection.title)
        assertThat(collection.videos).isNotEmpty
        collection.videos.map { video ->
            assertThat(video.videoId.uri.toString()).matches("https?://.*/videos/.*")
            assertThat(video.title).isNull()
            assertThat(video.description).isNull()
            assertThat(video.playback).isNull()
        }
    }

    @Test
    fun `get detailed Collection returns a collection with videos prefetched`() {
        val testCollection = getClient().myCollections.component1()

        val collection = getClient().getDetailed(testCollection.collectionId)

        assertThat(collection.title).isEqualTo(testCollection.title)
        assertThat(collection.videos).isNotEmpty
        collection.videos.map { preFetchedVideo ->
            val video = getClient().get(preFetchedVideo.videoId)

            assertThat(preFetchedVideo.videoId).isEqualTo(video.videoId)
            assertThat(preFetchedVideo.title)
                .isNotBlank()
                .isEqualTo(video.title)
            assertThat(preFetchedVideo.description)
                .isNotBlank()
                .isEqualTo(video.description)
            assertThat(preFetchedVideo.releasedOn)
                .isNotNull()
                .isEqualTo(video.releasedOn)
            assertThat(preFetchedVideo.createdBy)
                .isNotBlank()
                .isEqualTo(video.createdBy)
            assertThat(preFetchedVideo.playback).isNotNull
            assertThat(preFetchedVideo.playback.playbackId)
                .isNotBlank()
                .isEqualTo(video.playback.playbackId)
            assertThat(preFetchedVideo.playback.duration)
                .isNotNull()
                .isEqualTo(video.playback.duration)
            assertThat(preFetchedVideo.playback.thumbnailUrl)
                .isNotBlank()
                .isEqualTo(video.playback.thumbnailUrl)
        }
    }

    @Test
    fun `create a kaltura video gives a unique id`() {
        val contentPartnerId1 =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest())
                .value

        val id1 = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "entry-123",
                contentProviderId = contentPartnerId1
            )
        )

        val contentPartnerId2 =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest(name = "hello")).value

        val id2 = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "entry-123",
                contentProviderId = contentPartnerId2
            )
        )

        assertThat(id1.uri.toString()).contains("/videos/")
        assertThat(id1.uri.toString()).isNotEqualTo(id2.uri.toString())
    }

    @Test
    fun `create an existing video throws VideoExistsException`() {
        val contentPartnerId =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest())
                .value

        val aVideo =
            TestFactories.createCreateVideoRequest(playbackId = "entry-123", contentProviderId = contentPartnerId)
        getClient().createVideo(aVideo)

        assertThrows<VideoExistsException> {
            getClient().createVideo(aVideo)
        }
    }

    @Test
    fun `create an illegal video playback throws`() {
        val aVideo = TestFactories.createCreateVideoRequest(playbackId = "illegal-video")

        assertThrows<IllegalVideoRequestException> {
            getClient().createVideo(aVideo)
        }
    }

    @Test
    fun `create a youtube persists video`() {
        val contentPartnerId =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest())
                .value

        val id1 = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "ref-id-123",
                playbackProvider = PlaybackProvider.YOUTUBE,
                contentProviderId = contentPartnerId
            )
        )

        assertThat(getClient().get(id1)).isNotNull
    }

    @Test
    fun `lookup video by content partner id`() {
        val contentPartnerId =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest())
                .value

        val request = TestFactories.createCreateVideoRequest(
            contentProviderId = contentPartnerId,
            contentProviderVideoId = "123",
            playbackId = "entry-123"
        )

        getClient().createVideo(request)

        assertThat(getClient().existsByContentPartnerInfo(contentPartnerId, "123")).isTrue()
        assertThat(getClient().existsByContentPartnerInfo(contentPartnerId, "124")).isFalse()
    }

    @Test
    fun `lookup video by content partner id with URL reserved chars`() {
        val contentPartnerId =
            getClient().createContentPartner(TestFactories.createContentPartnerRequest())
                .value

        val request = TestFactories.createCreateVideoRequest(
            contentProviderId = contentPartnerId,
            contentProviderVideoId = "?#&SP-123",
            playbackId = "entry-123"
        )

        getClient().createVideo(request)

        assertThat(getClient().existsByContentPartnerInfo(contentPartnerId, "?#&SP-123")).isTrue()
    }

    @Test
    fun `obtain subjects`() {
        val subjects: List<Subject> = getClient().subjects

        assertThat(subjects).hasSize(2)
    }

    @Test
    fun `fetch own collections`() {
        val collections: List<Collection> = getClient().myCollections

        assertThat(collections).hasSize(2)
    }

    @Test
    fun `fetch own collections detailed returns collections with deep video information`() {
        val collections: List<Collection> = getClient().myCollectionsDetailed

        assertThat(collections)
            .hasSize(2)
            .flatExtracting("videos")
            .extracting("playback")
            .extracting("thumbnailUrl").allSatisfy {
                assertThat(it as String).isNotBlank()
            }
    }

    @Test
    fun `specify page size when fetching collections`() {
        val collections: List<Collection> =
            getClient().getMyCollections(VideoServiceClient.PageSpec.builder().pageSize(1).build())

        assertThat(collections).hasSize(1)
    }

    @Test
    fun `fetch another users collections`() {
        val videoId = saveVideo()

        setSecurityContext("anotheruser@boclips.com")
        val collection = createCollection(
            CreateCollectionRequest(
                title = "another user's collection",
                videos = listOf(videoId.value)
            )
        )
        val cookingSubject = subjectRepository.create("Cooking")
        updateCollection(collection.id.value, UpdateCollectionRequest(subjects = setOf(cookingSubject.id.value)))

        val collections: List<Collection> = getClient().getCollectionsByOwner("anotheruser@boclips.com")

        assertThat(collections).hasSize(1)
        assertThat(collections[0].title).isEqualTo("another user's collection")
        assertThat(collections[0].videos[0].videoId.uri.toString()).isNotBlank()
        assertThat(collections[0].subjects.first().id).isNotNull()
        assertThat(collections[0].subjects.first().name).isNotNull()
    }
}

internal class FakeVideoServiceClientContractTest : VideoServiceClientContractTest() {
    @Test
    fun `returns detailed collections`() {
        assertThat(getClient().collectionsDetailed).hasSize(3)
    }

    val fakeClient: FakeClient = VideoServiceClient.getFakeClient().apply {
        addIllegalPlaybackId("illegal-video")

        val maths = addSubject("Maths")
        addSubject("French")

        val createdContentPartnerId = createContentPartner(TestFactories.createContentPartnerRequest(name = "TED"))

        val videoId = createVideo(
            TestFactories.createCreateVideoRequest(
                title = "Phenomenal test video",
                description = "the description",
                playbackId = "test-playback-id",
                contentProviderId = createdContentPartnerId.value,
                contentProviderVideoId = "collection-video-id",
                subjects = setOf(maths.id.value)
            )
        )
        val videos = listOf(
            Video.builder()
                .videoId(videoId)
                .build()
        )

        addCollection(
            Collection.builder()
                .collectionId(TestFactories.createCollectionId())
                .title("first collection")
                .subjects(setOf(maths))
                .videos(videos)
                .build()
        )
        addCollection(
            Collection.builder()
                .collectionId(TestFactories.createCollectionId())
                .title("second collection")
                .subjects(emptySet())
                .videos(videos)
                .build()
        )
        addCollection(
            Collection.builder()
                .collectionId(TestFactories.createCollectionId())
                .title("another user's collection")
                .subjects(setOf(maths))
                .videos(videos)
                .build(),
            "anotheruser@boclips.com"
        )
    }

    override fun getClient() = fakeClient
}

internal class ApiVideoServiceClientContractTest : VideoServiceClientContractTest() {
    @BeforeEach
    fun setUp() {
        fakeKalturaClient.createMediaEntry(
            "entry-123",
            "ref-entry-123",
            Duration.ofMinutes(1),
            MediaEntryStatus.READY
        )
        fakeYoutubePlaybackProvider.addVideo("ref-id-123", "http://my-little-pony.com", Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("ref-id-123", "http://my-little-pony.com", "channelId")

        val mathsSubject = subjectRepository.create("Maths")
        val frenchSubject = subjectRepository.create("French")

        val videoId = saveVideo()

        setSecurityContext("user@boclips.com")
        createCollection(CreateCollectionRequest(title = "first collection", videos = listOf(videoId.value))).apply {
            updateCollection(this.id.value, UpdateCollectionRequest(subjects = setOf(mathsSubject.id.value)))
        }
        createCollection(CreateCollectionRequest(title = "second collection", videos = listOf(videoId.value))).apply {
            updateCollection(this.id.value, UpdateCollectionRequest(subjects = setOf(frenchSubject.id.value)))
        }
    }

    @BeforeEach
    fun cleanupContracts() {
        userServiceClient.clearContracts()
    }

    @Test
    fun `can retrieve collections I'm eligible to see with details`() {
        val videoId = saveVideo()

        val firstCollection = createCollection(
            CreateCollectionRequest(
                title = "First Contracted Collection",
                videos = listOf(videoId.value)
            )
        )

        val secondCollection = createCollection(
            CreateCollectionRequest(
                title = "Second Contracted Collection",
                videos = listOf(videoId.value)
            )
        )

        userServiceClient.addContract(SelectedContentContract().apply {
            collectionIds = listOf(firstCollection.id.value, secondCollection.id.value)
        })

        val collections: List<Collection> = getClient().collectionsDetailed

        assertThat(collections)
            .hasSize(2)
            .extracting("title")
            .containsExactlyInAnyOrder("First Contracted Collection", "Second Contracted Collection")

        assertThat(collections)
            .hasSize(2)
            .flatExtracting("videos")
            .extracting("playback")
            .extracting("thumbnailUrl").allSatisfy {
                assertThat(it as String).isNotBlank()
            }
    }

    override fun getClient() = VideoServiceClient.getBoclipsUser(videoServiceUrl)
}
