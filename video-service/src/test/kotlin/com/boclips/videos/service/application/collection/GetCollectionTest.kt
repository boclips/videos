package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetCollectionTest {

    lateinit var playbackToResourceConverter: PlaybackToResourceConverter
    lateinit var collectionRepository: CollectionRepository
    lateinit var collectionResourceFactory: CollectionResourceFactory
    lateinit var videoService: VideoService
    lateinit var videosLinkBuilder: VideosLinkBuilder

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { getPlayableVideo(any<List<VideoId>>()) } doReturn listOf(
                TestFactories.createVideo()
            )
        }
        videosLinkBuilder = mock()
        playbackToResourceConverter = PlaybackToResourceConverter(EventsLinkBuilder())
        collectionResourceFactory = CollectionResourceFactory(
            VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
            SubjectToResourceConverter(),
            videoService
        )
    }

    @Test
    fun `finding collection by ID`() {
        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(
            id = collectionId,
            owner = "me@me.com",
            title = "Freshly found"
        )

        collectionRepository = mock {
            on { find(collectionId) } doReturn onGetCollection
        }

        val collection = GetCollection(collectionRepository, collectionResourceFactory).invoke(collectionId.value)

        assertThat(collection.id).isEqualTo(onGetCollection.id.value)
        assertThat(collection.owner).isEqualTo(onGetCollection.owner.value)
        assertThat(collection.title).isEqualTo(onGetCollection.title)
        assertThat(collection.videos).isNotEmpty
        assertThat(collection.videos.component1().content.id).isNotBlank()
        assertThat(collection.videos.component1().content.title).isNull()
        assertThat(collection.videos.component1().content.description).isNull()
        assertThat(collection.videos.component1().content.playback).isNull()
    }

    @Test
    fun `finding collection by ID using details projection`() {
        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(
            id = collectionId,
            owner = "me@me.com",
            title = "Freshly found"
        )

        collectionRepository = mock {
            on { find(collectionId) } doReturn onGetCollection
        }

        val collection = GetCollection(collectionRepository, collectionResourceFactory).invoke(
            collectionId.value,
            Projection.details
        )

        assertThat(collection.id).isEqualTo(onGetCollection.id.value)
        assertThat(collection.owner).isEqualTo(onGetCollection.owner.value)
        assertThat(collection.title).isEqualTo(onGetCollection.title)
        assertThat(collection.videos).isNotEmpty
        assertThat(collection.videos.component1().content.id).isNotBlank()
        assertThat(collection.videos.component1().content.title).isNotBlank()
        assertThat(collection.videos.component1().content.description).isNotBlank()
        assertThat(collection.videos.component1().content.playback).isNotNull()
        assertThat(collection.videos.component1().content.playback?.content?.duration).isNotNull()
        assertThat(collection.videos.component1().content.playback?.content?.thumbnailUrl).isNotBlank()
    }

    @Test
    fun `throws not found error when collection doesn't exist`() {
        collectionRepository = mock {
            on { find(any()) } doAnswer { null }
        }

        val getCollection = GetCollection(collectionRepository, collectionResourceFactory)

        assertThrows<CollectionNotFoundException> { getCollection(collectionId = "123") }
    }

    @Test
    fun `throws error when user doesn't own the private collection`() {
        setSecurityContext("attacker@example.com")

        val privateCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = false)

        collectionRepository = mock {
            on { find(privateCollection.id) } doReturn privateCollection
        }

        val getCollection = GetCollection(collectionRepository, collectionResourceFactory)

        assertThrows<CollectionAccessNotAuthorizedException> { getCollection(collectionId = privateCollection.id.value) }
    }

    @Test
    fun `allows any teacher to access public collection`() {
        setSecurityContext("nosey@example.com")

        val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

        collectionRepository = mock {
            on { find(publicCollection.id) } doReturn publicCollection
        }

        val collection =
            GetCollection(collectionRepository, collectionResourceFactory).invoke(publicCollection.id.value)

        assertThat(collection.id).isEqualTo(publicCollection.id.value)
    }
}