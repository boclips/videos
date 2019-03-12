package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteCollectionTest {

    lateinit var collectionService: CollectionService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `deletes collection`() {
        collectionService = mock {
            on { getById(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val deleteCollection = DeleteCollection(collectionService)
        val collectionId = TestFactories.aValidId()

        deleteCollection(collectionId)

        verify(collectionService).delete(eq(CollectionId(collectionId)))
    }

    @Test
    fun `throws error when user doesn't own the private collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(id = collectionId, owner = "innocent@example.com", isPublic = false)

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val deleteCollection = DeleteCollection(collectionService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            deleteCollection(
                    collectionId = collectionId.value
            )
        }
        verify(collectionService, never()).update(any(), any<CollectionUpdateCommand>())
    }

    @Test
    fun `throws error when user doesn't own the public collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection = TestFactories.createCollection(id = collectionId, owner = "innocent@example.com", isPublic = true)

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val deleteCollection = DeleteCollection(collectionService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            deleteCollection(
                    collectionId = collectionId.value
            )
        }
        verify(collectionService, never()).update(any(), any<CollectionUpdateCommand>())
    }


    @Test
    fun `throws when collection doesn't exist`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection: Collection? = null

        collectionService = mock {
            on { getById(collectionId) } doReturn onGetCollection
        }

        val deleteCollection = DeleteCollection(collectionService)

        assertThrows<CollectionNotFoundException> {
            deleteCollection(
                    collectionId = collectionId.value
            )
        }
        verify(collectionService, never()).update(any(), any<CollectionUpdateCommand>())
    }
}