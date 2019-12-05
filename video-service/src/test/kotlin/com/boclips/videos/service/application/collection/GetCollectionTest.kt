package com.boclips.videos.service.application.collection

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionAccessService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.attachments.AttachmentToResourceConverter
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetCollectionTest {

    lateinit var playbackToResourceConverter: PlaybackToResourceConverter
    lateinit var collectionResourceFactory: CollectionResourceFactory
    lateinit var videoService: VideoService
    lateinit var collectionAccessService: CollectionAccessService
    lateinit var videosLinkBuilder: VideosLinkBuilder
    lateinit var attachmentsLinkBuilder: AttachmentsLinkBuilder
    lateinit var collectionRepository: CollectionRepository

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { getPlayableVideo(any<List<VideoId>>()) } doReturn listOf(
                TestFactories.createVideo()
            )
        }
        collectionAccessService = mock()
        videosLinkBuilder = mock()
        attachmentsLinkBuilder = mock()
        playbackToResourceConverter =
            PlaybackToResourceConverter(EventsLinkBuilder(), PlaybacksLinkBuilder(TestKalturaClient()))
        collectionResourceFactory = CollectionResourceFactory(
            VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
            SubjectToResourceConverter(),
            AttachmentToResourceConverter(attachmentsLinkBuilder),
            videoService
        )

        collectionRepository = mock()
    }

    @Test
    fun `finding collection by ID`() {
        val collectionId = CollectionId("collection-123")
        val mockCollection = TestFactories.createCollection(
            id = collectionId,
            owner = "me@me.com",
            title = "Freshly found",
            description = "My description"
        )

        collectionAccessService = mock {
            on { hasReadAccess(collectionId.value) } doReturn true
        }

        collectionRepository = mock {
            on { find(any()) } doReturn mockCollection
        }

        val collection = GetCollection(
            collectionResourceFactory,
            collectionAccessService,
            collectionRepository
        ).invoke(collectionId.value)

        assertThat(collection.id).isEqualTo(mockCollection.id.value)
        assertThat(collection.owner).isEqualTo(mockCollection.owner.value)
        assertThat(collection.title).isEqualTo(mockCollection.title)
        assertThat(collection.description).isEqualTo(mockCollection.description)
        assertThat(collection.videos).isNotEmpty
        assertThat(collection.videos.component1().content.id).isNotBlank()
        assertThat(collection.videos.component1().content.title).isNull()
        assertThat(collection.videos.component1().content.description).isNull()
        assertThat(collection.videos.component1().content.playback).isNull()
    }

    @Test
    fun `finding collection by ID using details projection`() {
        val collectionId = CollectionId("collection-123")
        val mockCollection = TestFactories.createCollection(
            id = collectionId,
            owner = "me@me.com",
            title = "Freshly found"
        )

        collectionAccessService = mock {
            on { hasReadAccess(collectionId.value) } doReturn true
        }

        collectionRepository = mock {
            on { find(any()) } doReturn mockCollection
        }

        val collection = GetCollection(collectionResourceFactory, collectionAccessService, collectionRepository).invoke(
            collectionId.value,
            Projection.details
        )

        assertThat(collection.id).isEqualTo(mockCollection.id.value)
        assertThat(collection.owner).isEqualTo(mockCollection.owner.value)
        assertThat(collection.title).isEqualTo(mockCollection.title)
        assertThat(collection.videos).isNotEmpty
        assertThat(collection.videos.component1().content.id).isNotBlank()
        assertThat(collection.videos.component1().content.title).isNotBlank()
        assertThat(collection.videos.component1().content.description).isNotBlank()
        assertThat(collection.videos.component1().content.playback).isNotNull()
        assertThat(collection.videos.component1().content.playback?.content?.duration).isNotNull()
        assertThat(collection.videos.component1().content.playback?.content?.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `propagates CollectionNotFound error thrown from downstream`() {
        collectionAccessService = mock {
            on { hasReadAccess(any()) } doThrow (CollectionNotFoundException("123"))
        }

        val getCollection = GetCollection(collectionResourceFactory, collectionAccessService, collectionRepository)

        assertThrows<CollectionNotFoundException> { getCollection(collectionId = "123") }
    }

    @Test
    fun `propagates the CollectionAccessNotAuthorizedException error thrown from downstream`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("test-collection-id")

        collectionAccessService = mock {
            on { hasReadAccess(collectionId.value) } doReturn false
        }

        val getCollection = GetCollection(collectionResourceFactory, collectionAccessService, collectionRepository)

        assertThrows<CollectionAccessNotAuthorizedException> { getCollection(collectionId.value) }
    }
}
