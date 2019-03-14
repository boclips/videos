package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
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

    lateinit var collectionService: CollectionService
    lateinit var collectionResourceFactory: CollectionResourceFactory
    lateinit var videoService: VideoService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { get(any<List<AssetId>>()) } doReturn listOf(
                TestFactories.createVideo()
            )
        }
        collectionResourceFactory = CollectionResourceFactory(VideoToResourceConverter(), videoService)
    }

    @Test
    fun `finding collection by ID`() {
        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(
            id = collectionId,
            owner = "me@me.com",
            title = "Freshly found"
        )

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val collection = GetCollection(collectionService, collectionResourceFactory).invoke(collectionId.value)

        assertThat(collection.id).isEqualTo(onGetCollection.id.value)
        assertThat(collection.owner).isEqualTo(onGetCollection.owner.value)
        assertThat(collection.title).isEqualTo(onGetCollection.title)
    }

    @Test
    fun `throws not found error when collection doesn't exist`() {
        collectionService = mock {
            on { getById(any()) } doAnswer { null }
        }

        val getCollection = GetCollection(collectionService, collectionResourceFactory)

        assertThrows<CollectionNotFoundException> { getCollection(collectionId = "123") }
        assertThrows<CollectionNotFoundException> { getCollection(collectionId = null) }
    }

    @Test
    fun `throws error when user doesn't own the private collection`() {
        setSecurityContext("attacker@example.com")

        val privateCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = false)

        collectionService = mock {
            on { getById(privateCollection.id) } doReturn privateCollection
        }

        val getCollection = GetCollection(collectionService, collectionResourceFactory)

        assertThrows<CollectionAccessNotAuthorizedException> { getCollection(collectionId = privateCollection.id.value) }
    }

    @Test
    fun `allows any teacher to access public collection`() {
        setSecurityContext("nosey@example.com")

        val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

        collectionService = mock {
            on { getById(publicCollection.id) } doReturn publicCollection
        }

        val collection = GetCollection(collectionService, collectionResourceFactory).invoke(publicCollection.id.value)

        assertThat(collection.id).isEqualTo(publicCollection.id.value)
    }
}