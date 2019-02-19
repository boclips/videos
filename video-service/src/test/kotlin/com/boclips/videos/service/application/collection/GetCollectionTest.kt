package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.setSecurityContext
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
    lateinit var collectionResourceConverter: CollectionResourceConverter

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        collectionResourceConverter = CollectionResourceConverter(VideoToResourceConverter())
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

        val collection = GetCollection(collectionService, collectionResourceConverter).invoke(collectionId.value)

        assertThat(collection.id).isEqualTo(onGetCollection.id.value)
        assertThat(collection.owner).isEqualTo(onGetCollection.owner.value)
        assertThat(collection.title).isEqualTo(onGetCollection.title)
    }

    @Test
    fun `throws not found error when collection doesn't exist`() {
        collectionService = mock {
            on { getById(any()) } doAnswer { null }
        }

        val getCollection = GetCollection(collectionService, collectionResourceConverter)

        assertThrows<CollectionNotFoundException> { getCollection(collectionId = "123") }
        assertThrows<CollectionNotFoundException> { getCollection(collectionId = null) }
    }

    @Test
    fun `throws error when user doesn't own the collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(id = collectionId, owner = "innocent@example.com")

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val getCollection = GetCollection(collectionService, collectionResourceConverter)

        assertThrows<CollectionAccessNotAuthorizedException> { getCollection(collectionId = collectionId.value) }
    }
}