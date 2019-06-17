package com.boclips.videos.service.client

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException
import com.boclips.videos.service.client.exceptions.VideoExistsException
import com.boclips.videos.service.client.testsupport.AbstractVideoServiceClientSpringIntegrationTest
import com.boclips.videos.service.client.testsupport.TestFactories
import com.boclips.videos.service.domain.model.subjects.SubjectRepository
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

internal abstract class VideoServiceClientContractTest : AbstractVideoServiceClientSpringIntegrationTest() {

    abstract fun getClient(): VideoServiceClient

    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Test
    fun `get a video`() {
        val id = getClient().create(
            TestFactories.createCreateVideoRequest(
                title = "the title",
                description = "the description",
                playbackId = "ref-id-123"
            )
        )

        val video = getClient().get(id)

        assertThat(video.title).isEqualTo("the title")
        assertThat(video.description).isEqualTo("the description")
    }

    @Test
    fun `get VideoId for raw identifier`() {
        val rawId = getClient().create(TestFactories.createCreateVideoRequest(playbackId = "ref-id-123")).uri.toString()
            .split('/').last()

        val id = getClient().rawIdToVideoId(rawId)

        assertThat(id.uri.toString()).matches("https?://.*/videos/$rawId")
    }

    @Test
    fun `get CollectionId for raw identifier`() {
        val rawId = "test-collection-identifier"

        val id = getClient().rawIdToCollectionId(rawId)

        assertThat(id.uri.toString()).matches("https?://.*/collections/$rawId")
    }

    @Test
    fun `create a kaltura video gives a unique id`() {
        val id1 = getClient().create(
            TestFactories.createCreateVideoRequest(
                playbackId = "ref-id-123",
                contentProviderId = "1"
            )
        )
        val id2 = getClient().create(
            TestFactories.createCreateVideoRequest(
                playbackId = "ref-id-123",
                contentProviderId = "2"
            )
        )

        assertThat(id1.uri.toString()).contains("/videos/")
        assertThat(id1.uri.toString()).isNotEqualTo(id2.uri.toString())
    }

    @Test
    fun `create an existing video throws VideoExistsException`() {
        val aVideo = TestFactories.createCreateVideoRequest(playbackId = "ref-id-123")
        getClient().create(aVideo)

        assertThrows<VideoExistsException> {
            getClient().create(aVideo)
        }
    }

    @Test
    fun `create an illegal video playback throws`() {
        val aVideo = TestFactories.createCreateVideoRequest(playbackId = "illegal-video")

        assertThrows<IllegalVideoRequestException> {
            getClient().create(aVideo)
        }
    }

    @Test
    fun `create a youtube persists video`() {
        val id1 = getClient().create(
            TestFactories.createCreateVideoRequest(
                playbackId = "ref-id-123",
                playbackProvider = PlaybackProvider.YOUTUBE
            )
        )

        assertThat(getClient().get(id1)).isNotNull
    }

    @Test
    fun `lookup video by content partner id`() {
        val request = TestFactories.createCreateVideoRequest(
            contentProviderId = "ted",
            contentProviderVideoId = "123",
            playbackId = "ref-id-123"
        )

        getClient().create(request)

        assertThat(getClient().existsByContentPartnerInfo("ted", "123")).isTrue()
        assertThat(getClient().existsByContentPartnerInfo("ted", "124")).isFalse()
    }

    @Test
    fun `lookup video by content partner id with URL reserved chars`() {
        val request = TestFactories.createCreateVideoRequest(
            contentProviderId = "irrelevant",
            contentProviderVideoId = "?#&SP-123",
            playbackId = "ref-id-123"
        )

        getClient().create(request)

        assertThat(getClient().existsByContentPartnerInfo("irrelevant", "?#&SP-123")).isTrue()
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
        assertThat(collections[0].videos[0].uri.toString()).isNotBlank()
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
        assertThat(collections[0].videos[0].uri.toString()).isNotBlank()
        assertThat(collections[0].subjects).containsExactly(SubjectId("Math"))
    }
}

internal class FakeVideoServiceClientContractTest : VideoServiceClientContractTest() {
    val fakeClient = VideoServiceClient.getFakeClient().apply {
        addIllegalPlaybackId("illegal-video")
        addSubject("Maths")
        addSubject("French")

        val subjects = setOf(SubjectId("Math"))
        val videos = listOf(TestFactories.createVideoId())

        addCollection(Collection.builder().title("first collection").subjects(subjects).videos(videos).build())
        addCollection(Collection.builder().title("second collection").subjects(emptySet()).videos(videos).build())
        addCollection(
            Collection.builder().title("another user's collection").subjects(subjects).videos(videos).build(),
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
