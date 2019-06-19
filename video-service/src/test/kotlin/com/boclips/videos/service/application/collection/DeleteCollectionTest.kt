package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteCollectionTest {

    lateinit var collectionRepository: CollectionRepository
    var collectionSearchService: CollectionSearchService = mock {
        on { removeFromSearch(any()) }.then {  }
    }

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `deletes collection`() {
        collectionRepository = mock {
            on { find(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService)
        val collectionId = TestFactories.aValidId()

        deleteCollection(collectionId)

        verify(collectionRepository).delete(eq(CollectionId(collectionId)))
    }

    @Test
    fun `removes collection from the search index`() {
        collectionRepository = mock {
            on { find(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService)
        val collectionId = TestFactories.aValidId()

        deleteCollection(collectionId)

        verify(collectionSearchService).removeFromSearch(collectionId)
    }

    @Test
    fun `throws error when user doesn't own the private collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection =
            TestFactories.createCollection(id = collectionId, owner = "innocent@example.com", isPublic = false)

        collectionRepository = mock {
            on { find(collectionId) } doReturn onGetCollection
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            deleteCollection(
                collectionId = collectionId.value
            )
        }
        verify(collectionRepository, never()).update(any(), any<CollectionUpdateCommand>())
    }

    @Test
    fun `throws error when user doesn't own the public collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val onGetCollection =
            TestFactories.createCollection(id = collectionId, owner = "innocent@example.com", isPublic = true)

        collectionRepository = mock {
            on { find(collectionId) } doReturn onGetCollection
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            deleteCollection(
                collectionId = collectionId.value
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
            on { find(collectionId) } doReturn onGetCollection
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService)

        assertThrows<CollectionNotFoundException> {
            deleteCollection(
                collectionId = collectionId.value
            )
        }
        verify(collectionRepository, never()).update(any(), any<CollectionUpdateCommand>())
    }
}