package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.fakes.FakeEventService
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RemoveVideoFromCollectionTest {

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `removes the video using the collection service`() {
        val collectionService = mock<CollectionService> {
            on { getById(any())}.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val removeVideoFromCollection = RemoveVideoFromCollection(collectionService, FakeEventService())

        val videoId = TestFactories.aValidId()
        removeVideoFromCollection("col-id", videoId)

        argumentCaptor<RemoveVideoFromCollectionCommand>().apply {
            verify(collectionService).update(eq(CollectionId("col-id")), capture())
            assertThat(firstValue.videoId.value).isEqualTo(videoId)
        }
    }

    @Test
    fun `logs an event`() {
        val collectionService = mock<CollectionService> {
            on { getById(any())}.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val eventService = FakeEventService()
        val removeVideoFromCollection = RemoveVideoFromCollection(collectionService, eventService)

        val videoId = TestFactories.aValidId()
        removeVideoFromCollection("col-id", videoId)

        assertThat(eventService.removeFromCollectionEvent().data.videoId).isEqualTo(videoId)
        assertThat(eventService.removeFromCollectionEvent().data.collectionId).isEqualTo("col-id")
        assertThat(eventService.removeFromCollectionEvent().user.id).isEqualTo("me@me.com")
    }

    @Test
    fun `throws error when user doesn't own the collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(id = collectionId, owner = "innocent@example.com")

        val collectionService = mock<CollectionService> {
            on { getById(any())}.thenReturn(onGetCollection)
        }

        val removeVideoFromCollection = RemoveVideoFromCollection(collectionService, FakeEventService())

        assertThrows<CollectionAccessNotAuthorizedException> { removeVideoFromCollection(
            collectionId = collectionId.value,
            videoId = TestFactories.aValidId()
        ) }
        verify(collectionService, never()).update(any(), any<CollectionUpdateCommand>())
    }
}