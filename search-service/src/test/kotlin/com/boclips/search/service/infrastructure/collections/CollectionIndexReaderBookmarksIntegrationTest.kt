package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderBookmarksIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter(esClient)
    }

    @Test
    fun `returns public and private collections of an owner`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    bookmarkedBy = setOf("juan")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    bookmarkedBy = setOf("juan", "alexia")
                )
            )
        )

        assertThat(
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(bookmarkedBy = "alexia")))
        )
            .containsExactlyInAnyOrder("2")
    }
}
