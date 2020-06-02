package com.boclips.videos.service.presentation.converters

import com.boclips.kalturaclient.clients.TestKalturaClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.service.domain.service.video.VideoRetrievalService
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.ContentWarningLinkBuilder
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
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

        val videoToResourceConverter = VideoToResourceConverter(
            videosLinkBuilder = VideosLinkBuilder(uriComponentsBuilderFactory),
            playbackToResourceConverter = PlaybackToResourceConverter(
                eventsLinkBuilder = EventsLinkBuilder(),
                playbacksLinkBuilder = PlaybacksLinkBuilder(TestKalturaClient())
            ),
            attachmentToResourceConverter = attachmentToResourceConverter,
            contentWarningToResourceConverter = ContentWarningToResourceConverter(ContentWarningLinkBuilder())
        )
        val collectionsLinkBuilder = mock<CollectionsLinkBuilder>()
        videoRetrievalService = mock()

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

    private fun createTestResource(projection: Projection): CollectionResource {
        val video = TestFactories.createVideo(
            title = videoTitle,
            videoId = videoId
        )

        val collection = TestFactories.createCollection(
            title = "Collection Title",
            videos = listOf(video.videoId)
        )

        `when`(videoRetrievalService.getPlayableVideos(any(), any())).doReturn(listOf(video))

        return resourceConverter.buildCollectionResource(
            collection = collection,
            projection = projection,
            user = UserFactory.sample()
        )
    }
}


