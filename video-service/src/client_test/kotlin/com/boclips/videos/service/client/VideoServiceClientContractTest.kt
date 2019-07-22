package com.boclips.videos.service.client

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException
import com.boclips.videos.service.client.exceptions.VideoExistsException
import com.boclips.videos.service.client.testsupport.AbstractVideoServiceClientSpringIntegrationTest
import com.boclips.videos.service.client.testsupport.TestFactories
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
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
    fun `get a video`() {
        val playbackId = "ref-id-123"
        val id = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                title = "the title",
                description = "the description",
                playbackId = playbackId,
                contentProvider = "test-content-partner"
            )
        )

        val video = getClient().get(id)

        assertThat(video.title).isEqualTo("the title")
        assertThat(video.description).isEqualTo("the description")
        assertThat(video.releasedOn).isCloseTo(LocalDate.now(), within(1, ChronoUnit.DAYS))
        assertThat(video.createdBy).isEqualTo("test-content-partner")
        assertThat(video.playback?.playbackId).isEqualTo(playbackId)
        assertThat(video.playback?.thumbnailUrl).isNotBlank()
        assertThat(video.playback?.duration).isNotNull()
    }

    @Test
    fun `404 error is thrown when requested video is not found`() {
        val realVideoUriString = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                title = "the title",
                description = "the description",
                playbackId = "ref-id-123"
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
        val rawId = getClient().createVideo(TestFactories.createCreateVideoRequest(playbackId = "ref-id-123")).uri.toString()
            .split('/').last()

        val id = getClient().rawIdToVideoId(rawId)

        assertThat(id.uri.toString()).matches("https?://.*/videos/$rawId")
    }

    @Test
    fun `get official content partners`() {
        getClient().createContentPartner(CreateContentPartnerRequest("ted", null))
        getClient().createContentPartner(CreateContentPartnerRequest("ted", "123"))

        val contentPartners = getClient().findOfficialContentPartner("ted")

        assertThat(contentPartners).hasSize(1)
        assertThat(contentPartners[0].name).isEqualTo("ted")
        assertThat(contentPartners[0].contentPartnerId).isNotNull()
        assertThat(contentPartners[0].official).isTrue()
    }

    @Test
    fun `get youtube content partners`() {
        getClient().createContentPartner(CreateContentPartnerRequest("ted", null))
        getClient().createContentPartner(CreateContentPartnerRequest("ted", "123"))

        val contentPartners = getClient().findContentPartnerByYoutubeChannelId("123")

        assertThat(contentPartners).hasSize(1)
        assertThat(contentPartners[0].name).isEqualTo("ted")
        assertThat(contentPartners[0].contentPartnerId).isNotNull()
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
        val id1 = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "ref-id-123",
                contentProvider = "1"
            )
        )
        val id2 = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "ref-id-123",
                contentProvider = "2"
            )
        )

        assertThat(id1.uri.toString()).contains("/videos/")
        assertThat(id1.uri.toString()).isNotEqualTo(id2.uri.toString())
    }

    @Test
    fun `create an existing video throws VideoExistsException`() {
        val aVideo = TestFactories.createCreateVideoRequest(playbackId = "ref-id-123")
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
        val id1 = getClient().createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "ref-id-123",
                playbackProvider = PlaybackProvider.YOUTUBE
            )
        )

        assertThat(getClient().get(id1)).isNotNull
    }

    @Test
    fun `lookup video by content partner id`() {
        val contentPartnerId = "5d319cf8871956b43f45eb83"
        val request = TestFactories.createCreateVideoRequest(
            contentProviderId = contentPartnerId,
            contentProviderVideoId = "123",
            playbackId = "ref-id-123"
        )

        getClient().createVideo(request)

        assertThat(getClient().existsByContentPartnerInfo(contentPartnerId, "123")).isTrue()
        assertThat(getClient().existsByContentPartnerInfo(contentPartnerId, "124")).isFalse()
    }

    @Test
    fun `lookup video by content partner id with URL reserved chars`() {
        val contentPartnerId = "5d319cf8871956b43f45eb83"
        val request = TestFactories.createCreateVideoRequest(
            contentProviderId = contentPartnerId,
            contentProviderVideoId = "?#&SP-123",
            playbackId = "ref-id-123"
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
        assertThat(collections[0].title).endsWith("collection")
        assertThat(collections[0].videos[0].videoId.uri.toString()).isNotBlank()
        assertThat(collections[0].subjects).containsAnyOf(SubjectId("Math"), SubjectId("French"))
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
                title = "a collection",
                videos = listOf(videoId.value)
            )
        )

        updateCollection(collection.id.value, UpdateCollectionRequest(subjects = setOf("Math")))

        val collections: List<Collection> = getClient().getCollectionsByOwner("anotheruser@boclips.com")

        assertThat(collections).hasSize(1)
        assertThat(collections[0].videos[0].videoId.uri.toString()).isNotBlank()
        assertThat(collections[0].subjects).containsExactly(SubjectId("Math"))
    }
}

internal class FakeVideoServiceClientContractTest : VideoServiceClientContractTest() {
    val fakeClient = VideoServiceClient.getFakeClient().apply {
        addIllegalPlaybackId("illegal-video")
        addSubject("Maths")
        addSubject("French")

        val videoId = createVideo(
            TestFactories.createCreateVideoRequest(
                title = "Phenomenal test video",
                description = "the description",
                playbackId = "test-playback-id",
                contentProviderVideoId = "collection-video-id"
            )
        )

        val subjects = setOf(SubjectId("Math"))
        val videos = listOf(
            Video.builder()
                .videoId(videoId)
                .build()
        )

        addCollection(
            Collection.builder()
                .collectionId(TestFactories.createCollectionId())
                .title("first collection")
                .subjects(subjects)
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
                .subjects(subjects)
                .videos(videos)
                .build(),
            "anotheruser@boclips.com"
        )
    }

    override fun getClient() = fakeClient
}

internal class ApiVideoServiceClientContractTest : VideoServiceClientContractTest() {
    val realClient = VideoServiceClient.getUnauthorisedApiClient("http://localhost:9876")

    @BeforeEach
    fun setUp() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-123",
                referenceId = "ref-id-123",
                duration = Duration.ofMinutes(1)
            )
        )
        fakeYoutubePlaybackProvider.addVideo("ref-id-123", "http://my-little-pony.com", Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("ref-id-123", "http://my-little-pony.com", "channelId")

        subjectRepository.create("Maths")
        subjectRepository.create("French")

        val videoId = saveVideo()

        setSecurityContext("user@boclips.com")
        createCollection(CreateCollectionRequest(title = "first collection", videos = listOf(videoId.value))).apply {
            updateCollection(this.id.value, UpdateCollectionRequest(subjects = setOf("Math")))
        }
        createCollection(CreateCollectionRequest(title = "second collection", videos = listOf(videoId.value))).apply {
            updateCollection(this.id.value, UpdateCollectionRequest(subjects = setOf("French")))
        }
    }

    override fun getClient() = realClient
}
