package com.boclips.videos.service.domain.service.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.service.IsContractedToView
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.UserContractService
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollectionServiceTest {
    lateinit var collectionService: CollectionService
    lateinit var collectionRepository: CollectionRepository
    lateinit var collectionSearchService: CollectionSearchService
    lateinit var userContractService: UserContractService
    lateinit var isContractedToView: IsContractedToView

    @BeforeEach
    fun setup() {
        collectionSearchService = mock()
        userContractService = mock()
        isContractedToView = mock()
    }

    @Test
    fun `throws not found error when collection doesn't exist`() {
        collectionRepository = mock {
            on { find(any()) } doAnswer { null }
        }

        collectionService =
            CollectionService(collectionRepository, collectionSearchService, userContractService, isContractedToView)

        assertThrows<CollectionNotFoundException> { collectionService.getReadableCollectionOrThrow(collectionId = "123") }
    }

    @Test
    fun `throws error when user doesn't own the private collection`() {
        setSecurityContext("attacker@example.com")

        val privateCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = false)

        collectionRepository = mock {
            on { find(privateCollection.id) } doReturn privateCollection
        }

        collectionService =
            CollectionService(collectionRepository, collectionSearchService, userContractService, isContractedToView)

        assertThrows<CollectionAccessNotAuthorizedException> { collectionService.getOwnedCollectionOrThrow(collectionId = privateCollection.id.value) }
    }

    @Test
    fun `throws error when user doesn't own the public collection`() {
        setSecurityContext("attacker@example.com")

        val publicCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = true)

        collectionRepository = mock {
            on { find(publicCollection.id) } doReturn publicCollection
        }

        collectionService =
            CollectionService(collectionRepository, collectionSearchService, userContractService, isContractedToView)

        assertThrows<CollectionAccessNotAuthorizedException> { collectionService.getOwnedCollectionOrThrow(collectionId = publicCollection.id.value) }
    }

    @Test
    fun `allows any teacher to access public collection`() {
        setSecurityContext("nosey@example.com")

        val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

        collectionRepository = mock {
            on { find(publicCollection.id) } doReturn publicCollection
        }

        collectionService =
            CollectionService(collectionRepository, collectionSearchService, userContractService, isContractedToView)

        val collection = collectionService.getReadableCollectionOrThrow(publicCollection.id.value)

        Assertions.assertThat(collection.id).isEqualTo(publicCollection.id)
    }
}