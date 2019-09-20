package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.infrastructure.contract.CollectionSearchServiceFake
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.infrastructure.search.DefaultCollectionSearch
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RebuildCollectionIndexTest {

    lateinit var searchService: CollectionSearchService

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = CollectionSearchServiceFake()
        searchService = DefaultCollectionSearch(inMemorySearchService, inMemorySearchService)
    }

    @Test
    fun `rebuilds search index`() {
        val collectionId1 = CollectionId(TestFactories.aValidId())
        val collectionId2 = CollectionId(TestFactories.aValidId())
        val collectionId3 = CollectionId(TestFactories.aValidId())

        searchService.upsert(sequenceOf(TestFactories.createCollection(id = collectionId1, isPublic = true)))

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

        val rebuildSearchIndex = RebuildCollectionIndex(collectionRepository, searchService)

        assertThat(rebuildSearchIndex()).isCompleted.hasNotFailed()

        val searchRequest = PaginatedSearchRequest(CollectionQuery(phrase = "collection", visibility = CollectionVisibility.ALL))

        assertThat(searchService.search(searchRequest)).containsExactlyInAnyOrder(
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

        val rebuildSearchIndex = RebuildCollectionIndex(collectionRepository, searchService)

        assertThat(rebuildSearchIndex()).hasFailedWithThrowableThat().hasMessage("Boom")
    }
}
