package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderPermissionsIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `is not permitted to retrieve any collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Cat Dancing"),
                SearchableCollectionMetadataFactory.create(id = "2", title = "Beautiful Dog Dancing")
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        phrase = "Beautiful",
                        permittedIds = emptyList()
                    )
                )
            )

        Assertions.assertThat(results.elements).isEmpty()
    }

    @Test
    fun `is only permitted to retrieve specific collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Cat Dancing"),
                SearchableCollectionMetadataFactory.create(id = "2", title = "Beautiful Dog Dancing")
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        phrase = "Beautiful",
                        permittedIds = listOf("1")
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `is permitted to retrieve any collection`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Cat Dancing"),
                SearchableCollectionMetadataFactory.create(id = "2", title = "Beautiful Dog Dancing")
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        phrase = "Beautiful"
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("1", "2")
    }
}
