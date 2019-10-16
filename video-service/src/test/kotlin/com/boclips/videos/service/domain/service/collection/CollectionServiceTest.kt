package com.boclips.videos.service.domain.service.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.AccessRule
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.domain.service.CollectionAccessRule
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollectionServiceTest {
    lateinit var collectionService: CollectionService
    lateinit var collectionRepository: CollectionRepository
    lateinit var collectionSearchService: CollectionSearchService
    lateinit var accessRuleService: AccessRuleService

    @BeforeEach
    fun setup() {
        collectionSearchService = mock()
        accessRuleService = mock()
    }

    @Test
    fun `throws not found error when collection doesn't exist`() {
        collectionRepository = mock {
            on { find(any()) } doAnswer { null }
        }

        collectionService =
            CollectionService(collectionRepository, collectionSearchService, accessRuleService)

        assertThrows<CollectionNotFoundException> { collectionService.getReadableCollectionOrThrow(collectionId = "123") }
    }

    @Test
    fun `throws error when user doesn't own the private collection`() {
        accessRuleService = mock {
            on { getRules(any()) } doReturn AccessRule(CollectionAccessRule.specificIds(listOf()))
        }

        setSecurityContext("attacker@example.com")

        val privateCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = false)

        collectionRepository = mock {
            on { find(privateCollection.id) } doReturn privateCollection
        }

        collectionService =
            CollectionService(collectionRepository, collectionSearchService, accessRuleService)

        assertThrows<CollectionAccessNotAuthorizedException> { collectionService.getOwnedCollectionOrThrow(collectionId = privateCollection.id.value) }
    }

    @Test
    fun `throws error when user doesn't own the public collection`() {
        accessRuleService = mock {
            on { getRules(any()) } doReturn AccessRule(CollectionAccessRule.specificIds(listOf()))
        }

        setSecurityContext("attacker@example.com")

        val publicCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = true)

        collectionRepository = mock {
            on { find(publicCollection.id) } doReturn publicCollection
        }

        collectionService =
            CollectionService(collectionRepository, collectionSearchService, accessRuleService)

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
            CollectionService(collectionRepository, collectionSearchService, accessRuleService)

        val collection = collectionService.getReadableCollectionOrThrow(publicCollection.id.value)

        assertThat(collection.id).isEqualTo(publicCollection.id)
    }
}
