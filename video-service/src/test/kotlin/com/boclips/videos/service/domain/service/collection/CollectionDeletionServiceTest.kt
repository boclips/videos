package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.videos.service.application.collection.DeleteCollection
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class CollectionDeletionServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var deleteCollection: DeleteCollection

    @Test
    fun `deletes collection`() {
        val collectionId = saveCollection(owner = "me@me.com")

        deleteCollection.invoke(collectionId = collectionId.value, user = UserFactory.sample(id = "me@me.com"))

        assertThat(collectionRepository.find(collectionId)).isNull()
    }

    @Test
    fun `removes collection from the search index`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "An excellent collection")

        deleteCollection.invoke(collectionId.value, UserFactory.sample(id = "me@me.com"))

        val results =
            collectionIndexFake.search(PaginatedIndexSearchRequest(query = CollectionQuery(phrase = "An excellent")))

        assertThat(results.counts.totalHits).isEqualTo(0)
    }

    @Test
    fun `propagates errors when caller is not allowed to access the collection`() {
        val collectionId = saveCollection(owner = "alice@notme.com")

        assertThrows<CollectionNotFoundException> {
            deleteCollection(
                collectionId = collectionId.value,
                user = UserFactory.sample(id = "rob@me.com")
            )
        }
    }

    @Test
    fun `propagates errors when collection doesn't exist`() {
        assertThrows<CollectionNotFoundException> {
            deleteCollection(
                collectionId = "collection-123",
                user = UserFactory.sample()
            )
        }
    }
}
