package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteCollectionTest {
    lateinit var collectionRepository: CollectionRepository
    lateinit var collectionService: CollectionService
    private var collectionSearchService: CollectionSearchService = mock {
        on { removeFromSearch(any()) }.then { }
    }

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        collectionService = mock()
    }

    @Test
    fun `deletes collection`() {
        collectionRepository = mock {
            on { find(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService, collectionService)
        val collectionId = TestFactories.aValidId()

        deleteCollection(collectionId)

        verify(collectionRepository).delete(eq(CollectionId(collectionId)))
    }

    @Test
    fun `removes collection from the search index`() {
        collectionRepository = mock {
            on { find(any()) }.thenReturn(TestFactories.createCollection(owner = "me@me.com"))
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService, collectionService)
        val collectionId = TestFactories.aValidId()

        deleteCollection(collectionId)

        verify(collectionSearchService).removeFromSearch(collectionId)
    }

    @Test
    fun `propagates errors when caller is allowed to access the collection`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")
        val collection =
            TestFactories.createCollection(id = collectionId, owner = "innocent@example.com", isPublic = false)

        collectionRepository = mock {
            on { find(collectionId) } doReturn collection
        }
        collectionService = mock() {
            on { getOwnedCollectionOrThrow(collectionId.value) } doThrow (CollectionAccessNotAuthorizedException(
                UserId("attacker@example.com"),
                collectionId.value
            ))
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService, collectionService)

        assertThrows<CollectionAccessNotAuthorizedException> {
            deleteCollection(
                collectionId = collectionId.value
            )
        }

        verify(collectionRepository, never()).delete(any())
    }

    @Test
    fun `propagates errors when collection doesn't exist`() {
        setSecurityContext("attacker@example.com")

        val collectionId = CollectionId("collection-123")

        collectionRepository = mock()
        collectionService = mock() {
            on { getOwnedCollectionOrThrow(collectionId.value) } doThrow (CollectionNotFoundException(collectionId.value))
        }

        val deleteCollection = DeleteCollection(collectionRepository, collectionSearchService, collectionService)

        assertThrows<CollectionNotFoundException> {
            deleteCollection(
                collectionId = collectionId.value
            )
        }
        verify(collectionRepository, never()).delete(any())
    }
}
