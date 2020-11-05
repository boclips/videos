package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderSearchabilityIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `returns searchable collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "Beautiful Boy Dancing",
                    searchable = true
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "Beautiful Dog Barking",
                    searchable = false
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = CollectionQuery(owner = null, searchable = true)
                )
            )

        Assertions.assertThat(results.elements).containsExactly("100")
    }

    @Test
    fun `returns unsearchable collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "Beautiful Boy Dancing",
                    searchable = true
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "Beautiful Dog Barking",
                    searchable = false
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = CollectionQuery(searchable = false)
                )
            )

        Assertions.assertThat(results.elements).containsExactly("101")
    }

    @Test
    fun `returns all collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "Beautiful Boy Dancing",
                    searchable = true
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "Beautiful Dog Barking",
                    searchable = false
                )
            )
        )

        val results = collectionIndexReader.search(
            PaginatedIndexSearchRequest(
                query = CollectionQuery()
            )
        )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("100", "101")
    }

    @Test
    fun `permitted ids overwrite searchable`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Gentleman Dancing",
                    searchable = false
                )
            )
        )

        val result = collectionIndexReader.search(
            PaginatedIndexSearchRequest(
                query = CollectionQuery(
                    phrase = "Gentleman",
                    permittedIds = listOf("1")
                )
            )
        )

        Assertions.assertThat(result.elements).hasSize(1)
        Assertions.assertThat(result.counts.totalHits).isEqualTo(1)
    }
}
