package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.fakes.FakeAnalyticsEventService
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddVideoToCollectionTest {

    lateinit var collectionRepository: CollectionRepository

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `creates a collection if it doesn't exist`() {
        collectionRepository = mock {
            on { getById(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val addVideoToCollection = AddVideoToCollection(collectionRepository, mock())
        val collectionId = TestFactories.aValidId()
        val videoId = TestFactories.aValidId()

        addVideoToCollection(collectionId, videoId)

        argumentCaptor<CollectionUpdateCommand.AddVideoToCollectionCommand>().apply {
            verify(collectionRepository).update(eq(CollectionId(collectionId)), capture())
            assertThat(firstValue.videoId.value).isEqualTo(videoId)
        }
    }

    @Test
    fun `logs an event`() {
        collectionRepository = mock {
            on { getById(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val eventService = FakeAnalyticsEventService()
        val addVideoToCollection = AddVideoToCollection(collectionRepository, eventService)
        val collectionId = TestFactories.aValidId()
        val videoId = TestFactories.aValidId()

        addVideoToCollection(collectionId, videoId)

        assertThat(eventService.addToCollectionEvent().data.collectionId).isEqualTo(collectionId)
        assertThat(eventService.addToCollectionEvent().data.videoId).isEqualTo(videoId)
        assertThat(eventService.addToCollectionEvent().user.id).isEqualTo("me@me.com")
    }

    @Test
    fun `throws error when user doesn't own the collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(id = collectionId, owner = "innocent@example.com")

        collectionRepository = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val addToCollection = AddVideoToCollection(collectionRepository, FakeAnalyticsEventService())

        assertThrows<CollectionAccessNotAuthorizedException> {
            addToCollection(
                collectionId = collectionId.value,
                videoId = TestFactories.aValidId()
            )
        }
        verify(collectionRepository, never()).update(any(), any<CollectionUpdateCommand>())
    }
}