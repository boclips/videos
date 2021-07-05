package com.boclips.videos.service.presentation.converters

import com.boclips.kalturaclient.clients.TestKalturaClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.service.application.video.VideoRetrievalService
import com.boclips.videos.service.presentation.hateoas.*
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.web.util.UriComponentsBuilder

class CollectionResourceConverterTest {
    private lateinit var resourceConverter: CollectionResourceConverter
    private lateinit var videoRetrievalService: VideoRetrievalService

    private val videoId = TestFactories.createVideoId().value
    private val videoTitle = "Some Video Title"

    @BeforeEach
    fun setUp() {
        val attachmentToResourceConverter = AttachmentToResourceConverter(AttachmentsLinkBuilder())

        val uriComponentsBuilderFactory = mock<UriComponentsBuilderFactory> {
            on { getInstance() } doReturn UriComponentsBuilder.newInstance().path("videos")
        }
        videoRetrievalService = mock()

        val videoToResourceConverter = VideoToResourceConverter(
            videosLinkBuilder = VideosLinkBuilder(uriComponentsBuilderFactory),
            playbackToResourceConverter = PlaybackToResourceConverter(
                eventsLinkBuilder = EventsLinkBuilder(),
                playbacksLinkBuilder = PlaybacksLinkBuilder(TestKalturaClient())
            ),
            attachmentToResourceConverter = attachmentToResourceConverter,
            contentWarningToResourceConverter = ContentWarningToResourceConverter(ContentWarningLinkBuilder()),
            mock(),
            mock(),
            mock(),
            mock()
        )

        val collectionsLinkBuilder = CollectionsLinkBuilder(uriComponentsBuilderFactory)

        resourceConverter = CollectionResourceConverter(
            videoToResourceConverter,
            attachmentToResourceConverter,
            collectionsLinkBuilder,
            videoRetrievalService
        )
    }

    @Test
    fun `convert collection with list projection`() {
        val resource = createTestResource(Projection.list)

        assertThat(resource).isNotNull
        assertThat(resource.title).isEqualTo("Collection Title")

        assertThat(resource.videos.map { it.id }).containsExactly(videoId)
        assertThat(resource.videos[0].title).isNull()
    }

    @Test
    fun `convert collection with details projection`() {
        val resource = createTestResource(Projection.details)

        assertThat(resource).isNotNull
        assertThat(resource.title).isEqualTo("Collection Title")

        assertThat(resource.videos.map { it.id }).containsExactly(videoId)
        assertThat(resource.videos[0].title).isEqualTo(videoTitle)
    }

    @Test
    fun `convert collection with full projection`() {
        val resource = createTestResource(Projection.full)

        assertThat(resource).isNotNull
        assertThat(resource.title).isEqualTo("Collection Title")

        assertThat(resource.videos.map { it.id }).containsExactly(videoId)
        assertThat(resource.videos[0].title).isEqualTo(videoTitle)
    }

    @Test
    fun `contains the correct links`() {
        val resource = createTestResource(projection = Projection.list, collectionBelongsToUser = false)

        assertThat(resource._links!!["self"]).isNotNull
        assertThat(resource._links!!["bookmark"]).isNotNull
        assertThat(resource._links!!["interactedWith"]).isNotNull
    }

    @Test
    fun `contains the correct links for collection owners`() {
        val resource = createTestResource(projection = Projection.list, collectionBelongsToUser = true)

        assertThat(resource._links!!["self"]).isNotNull
        assertThat(resource._links!!["edit"]).isNotNull
        assertThat(resource._links!!["remove"]).isNotNull
        assertThat(resource._links!!["addVideo"]).isNotNull
        assertThat(resource._links!!["removeVideo"]).isNotNull
        assertThat(resource._links!!["interactedWith"]).isNotNull
    }

    @Test
    fun `contains the correct links for watch later collections`() {
        val resource =
            createTestResource(projection = Projection.list, collectionBelongsToUser = true, isDefault = true)

        assertThat(resource._links!!["self"]).isNotNull
        assertThat(resource._links!!["addVideo"]).isNotNull
        assertThat(resource._links!!["removeVideo"]).isNotNull
        assertThat(resource._links!!["interactedWith"]).isNotNull

        assertThat(resource._links!!["edit"]).isNull()
        assertThat(resource._links!!["remove"]).isNull()
    }

    private fun createTestResource(
        projection: Projection,
        collectionBelongsToUser: Boolean = false,
        isDefault: Boolean = false
    ): CollectionResource {
        val user = UserFactory.sample()

        val video = TestFactories.createVideo(
            title = videoTitle,
            videoId = videoId
        )

        val collection = TestFactories.createCollection(
            owner = if (collectionBelongsToUser) user.id!!.value else "random-user",
            title = "Collection Title",
            videos = listOf(video.videoId),
            default = isDefault,
            bookmarks = emptySet()
        )

        `when`(videoRetrievalService.getPlayableVideos(any(), any())).doReturn(listOf(video))

        return resourceConverter.buildCollectionResource(
            collection = collection,
            projection = projection,
            user = user
        )
    }
}
