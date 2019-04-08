package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.fakes.FakeAnalyticsEventService
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookmarkCollectionTest {

    lateinit var collectionService: CollectionService
    val eventService = FakeAnalyticsEventService()

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `updates on collection delegate`() {
        val collection = TestFactories.createCollection(owner = "other@me.com", isPublic = true)
        collectionService = mock {
            on { getById(any()) }.thenReturn(collection)
        }

        val bookmark = BookmarkCollection(collectionService, eventService)

        bookmark(collection.id.value)

        argumentCaptor<String>().apply {
            verify(collectionService).bookmark(eq(collection.id), eq(UserId("me@me.com")))
        }
    }

    @Test
    fun `throws error when user owns the collection`() {
        setSecurityContext("me@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection =
            TestFactories.createCollection(id = collectionId, owner = "me@example.com", isPublic = true)

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val bookmark = BookmarkCollection(collectionService, eventService)

        assertThrows<CollectionIllegalOperationException> {
            bookmark(
                collectionId = collectionId.value
            )
        }
        verify(collectionService, never()).bookmark(any(), any())
    }

    @Test
    fun `throws error when collection is not public`() {
        setSecurityContext("me@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection =
            TestFactories.createCollection(id = collectionId, owner = "other@example.com", isPublic = false)

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val bookmark = BookmarkCollection(collectionService, eventService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            bookmark(
                collectionId = collectionId.value
            )
        }
        verify(collectionService, never()).bookmark(any(), any())
    }

    @Test
    fun `throws when collection doesn't exist`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection: Collection? = null

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val bookmark = BookmarkCollection(collectionService, eventService)

        assertThrows<CollectionNotFoundException> {
            bookmark(
                collectionId = collectionId.value
            )
        }
        verify(collectionService, never()).bookmark(any(), any())
    }

    @Test
    fun `logs an event`() {
        val collection = TestFactories.createCollection(owner = "other@me.com", isPublic = true)
        collectionService = mock {
            on { getById(any()) }.thenReturn(collection)
        }

        val bookmark = BookmarkCollection(collectionService, eventService)

        bookmark(collection.id.value)

        Assertions.assertThat(eventService.bookmarkEvent().data.collectionId).isEqualTo(collection.id.value)
        Assertions.assertThat(eventService.bookmarkEvent().user.id).isEqualTo("me@me.com")
    }
}