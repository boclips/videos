package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderOwnerIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `returns public and private collections of an owner`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    owner = "juan-123"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    owner = "juan-123"
                ),
                SearchableCollectionMetadataFactory.create(id = "3", owner = "pablo-123")
            )
        )

        val results = collectionIndexReader.search(
            PaginatedIndexSearchRequest(
                query = CollectionQuery(
                    owner = "juan-123",
                    searchable = null
                )
            )
        )
        assertThat(results.elements).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `returns no collections if it does not match the owner`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    owner = "juan"
                )
            )
        )

        val results = collectionIndexReader.search(
            PaginatedIndexSearchRequest(
                query = CollectionQuery(
                    owner = "jose",
                    searchable = null
                )
            )
        )

        assertThat(results.elements).isEmpty()
    }
}
