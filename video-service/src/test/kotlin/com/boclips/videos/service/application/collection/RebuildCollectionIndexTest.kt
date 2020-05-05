package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.infrastructure.contract.CollectionSearchServiceFake
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.infrastructure.search.DefaultCollectionSearch
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RebuildCollectionIndexTest {

    lateinit var index: CollectionIndex

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = CollectionSearchServiceFake()
        index = DefaultCollectionSearch(inMemorySearchService, inMemorySearchService)
    }

    @Test
    fun `rebuilds search index`() {
        val collectionId1 = CollectionId(TestFactories.aValidId())
        val collectionId2 = CollectionId(TestFactories.aValidId())
        val collectionId3 = CollectionId(TestFactories.aValidId())

        index.upsert(sequenceOf(TestFactories.createCollection(id = collectionId1, isPublic = true)))

        val collectionRepository = mock<CollectionRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<Collection>) -> Unit

                consumer(
                    sequenceOf(
                        TestFactories.createCollection(id = collectionId2, title = "collection", isPublic = true),
                        TestFactories.createCollection(id = collectionId3, title = "collection", isPublic = false)
                    )
                )
            }
        }

        val rebuildSearchIndex = RebuildCollectionIndex(collectionRepository, index)

        rebuildSearchIndex()

        val results = index.search(PaginatedSearchRequest(CollectionQuery(phrase = "collection")))

        assertThat(results.elements).containsExactlyInAnyOrder(
            collectionId2.value,
            collectionId3.value
        )
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val collectionRepository = mock<CollectionRepository> {
            on {
                streamAll(any())
            } doThrow (MongoClientException("Boom"))
        }

        val rebuildSearchIndex = RebuildCollectionIndex(collectionRepository, index)

        assertThrows<MongoClientException> {
            rebuildSearchIndex()
        }
    }
}
