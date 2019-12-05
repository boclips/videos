package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class DeleteCollectionTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var deleteCollection: DeleteCollection

    @Test
    fun `deletes collection`() {
        setSecurityContext("me@me.com")
        val collectionId = saveCollection(owner = "me@me.com")

        deleteCollection.invoke(collectionId.value)

        assertThat(collectionRepository.find(collectionId)).isNull()
    }

    @Test
    fun `removes collection from the search index`() {
        setSecurityContext("me@me.com")
        val collectionId = saveCollection(owner = "me@me.com", title = "An excellent collection")

        deleteCollection.invoke(collectionId.value)

        assertThat(collectionSearchService.count(CollectionQuery(phrase = "An excellent"))).isEqualTo(0)
    }

    @Test
    fun `propagates errors when caller is not allowed to access the collection`() {
        setSecurityContext("rob@me.com")

        val collectionId = saveCollection(owner = "alice@notme.com")

        assertThrows<CollectionAccessNotAuthorizedException> {
            deleteCollection(collectionId = collectionId.value)
        }
    }

    @Test
    fun `propagates errors when collection doesn't exist`() {
        setSecurityContext("rob@me.com")

        assertThrows<CollectionNotFoundException> {
            deleteCollection(
                collectionId = "collection-123"
            )
        }
    }
}
