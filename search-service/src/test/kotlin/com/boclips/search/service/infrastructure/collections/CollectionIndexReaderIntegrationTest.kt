package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `can retrieve collections exactly`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing"))
        )

        val results =
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(phrase = "Beautiful Boy Dancing")))

        Assertions.assertThat(results).containsExactly("1")
    }

    @Test
    fun `can retrieve collections word matching`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "Cheeky Boy Dancing"),
                SearchableCollectionMetadataFactory.create(id = "2", title = "Cheeky Girl Dancing")
            )
        )

        val results =
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(phrase = "Boy")))

        Assertions.assertThat(results).containsExactly("1")
    }

    @Test
    fun `does not multi match word parts`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "Cold War"),
                SearchableCollectionMetadataFactory.create(id = "2", title = "The Great War WWI")
            )
        )

        val results =
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(phrase = "ear")))

        Assertions.assertThat(results).isEmpty()
    }

    @Test
    fun `cannot retrieve collections part-word matching`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "100", title = "Beautiful Boy Dancing"))
        )

        val results =
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(phrase = "Boi")))

        Assertions.assertThat(results).containsExactly("100")
    }

    @Test
    fun `returns collections with respecting sorting`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "Beautiful Boy Dancing",
                    hasAttachments = false
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "Beautiful Dog Barking",
                    hasAttachments = true
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        sort = Sort(
                            CollectionMetadata::hasAttachments,
                            SortOrder.DESC
                        )
                    )
                )
            )

        Assertions.assertThat(results).containsExactly("101", "100")
    }
}
