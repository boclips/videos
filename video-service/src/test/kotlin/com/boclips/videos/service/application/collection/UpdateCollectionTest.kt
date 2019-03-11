package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.setSecurityContext
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

    lateinit var collectionService: CollectionService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `renames collection`() {
        collectionService = mock {
            on { getById(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val renameCollection = UpdateCollection(collectionService)
        val collectionId = TestFactories.aValidId()

        renameCollection(collectionId, UpdateCollectionRequest(title = "new title"))

        argumentCaptor<RenameCollectionCommand>().apply {
            verify(collectionService).update(eq(CollectionId(collectionId)), capture())
            assertThat(firstValue.title).isEqualTo("new title")
        }
    }

    @Test
    fun `throws error when user doesn't own the collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(id = collectionId, owner = "innocent@example.com")

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val renameCollection = UpdateCollection(collectionService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            renameCollection(
                collectionId = collectionId.value,
                updateCollectionRequest = UpdateCollectionRequest(title = "new title")
            )
        }
        verify(collectionService, never()).update(any(), any())
    }

    @Test
    fun `throws when collection doesn't exist`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection: Collection? = null

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val renameCollection = UpdateCollection(collectionService)

        assertThrows<CollectionNotFoundException> {
            renameCollection(
                collectionId = collectionId.value,
                updateCollectionRequest = UpdateCollectionRequest(title = "new title")
            )
        }
        verify(collectionService, never()).update(any(), any())
    }
}