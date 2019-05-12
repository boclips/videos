package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
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

class UpdateCollectionTest {

    lateinit var collectionRepository: CollectionRepository

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `updates on collection delegate`() {
        collectionRepository = mock {
            on { getById(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val renameCollection = UpdateCollection(collectionRepository, FakeAnalyticsEventService())
        val collectionId = TestFactories.aValidId()

        renameCollection(collectionId, UpdateCollectionRequest(title = "new title"))

        argumentCaptor<CollectionUpdateCommand.RenameCollectionCommand>().apply {
            verify(collectionRepository).update(eq(CollectionId(collectionId)), any<List<CollectionUpdateCommand>>())
        }
    }

    @Test
    fun `logs an event for renaming`() {
        collectionRepository = mock {
            on { getById(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val eventService = FakeAnalyticsEventService()
        val renameCollection = UpdateCollection(collectionRepository, eventService)
        val collectionId = TestFactories.aValidId()

        renameCollection(collectionId, UpdateCollectionRequest(title = "new title"))

        assertThat(eventService.renameCollectionEvent().data.collectionId).isEqualTo(collectionId)
        assertThat(eventService.renameCollectionEvent().user.id).isEqualTo("me@me.com")
        assertThat(eventService.renameCollectionEvent().data.collectionTitle).isEqualTo("new title")
    }

    @Test
    fun `logs an event for changing visiblity`() {
        collectionRepository = mock {
            on { getById(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val eventService = FakeAnalyticsEventService()
        val renameCollection = UpdateCollection(collectionRepository, eventService)
        val collectionId = TestFactories.aValidId()

        renameCollection(collectionId, UpdateCollectionRequest(isPublic = true))

        assertThat(eventService.changeVisibilityOfCollectionEvent().data.collectionId).isEqualTo(collectionId)
        assertThat(eventService.changeVisibilityOfCollectionEvent().user.id).isEqualTo("me@me.com")
        assertThat(eventService.changeVisibilityOfCollectionEvent().data.isPublic).isTrue()
    }

    @Test
    fun `throws error when user doesn't own the collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(id = collectionId, owner = "innocent@example.com")

        collectionRepository = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val renameCollection = UpdateCollection(collectionRepository, FakeAnalyticsEventService())

        assertThrows<CollectionAccessNotAuthorizedException> {
            renameCollection(
                collectionId = collectionId.value,
                updateCollectionRequest = UpdateCollectionRequest(title = "new title")
            )
        }
        verify(collectionRepository, never()).update(any(), any<CollectionUpdateCommand>())
    }

    @Test
    fun `throws when collection doesn't exist`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection: Collection? = null

        collectionRepository = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val renameCollection = UpdateCollection(collectionRepository, FakeAnalyticsEventService())

        assertThrows<CollectionNotFoundException> {
            renameCollection(
                collectionId = collectionId.value,
                updateCollectionRequest = UpdateCollectionRequest(title = "new title")
            )
        }
        verify(collectionRepository, never()).update(any(), any<CollectionUpdateCommand>())
    }
}
