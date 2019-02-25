package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RenameCollectionCommand
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RenameCollectionTest {

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

        val renameCollection = RenameCollection(collectionService)
        val collectionId = TestFactories.aValidId()

        renameCollection(collectionId, "new title")

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

        val renameCollection = RenameCollection(collectionService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            renameCollection(
                    collectionId = collectionId.value,
                    title = "new title"
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

        val renameCollection = RenameCollection(collectionService)

        assertThrows<CollectionNotFoundException> {
            renameCollection(
                    collectionId = collectionId.value,
                    title = "new title"
            )
        }
        verify(collectionService, never()).update(any(), any())
    }
}