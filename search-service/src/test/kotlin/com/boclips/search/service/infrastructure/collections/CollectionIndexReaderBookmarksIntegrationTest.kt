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
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `returns bookmarked collections for a user`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    bookmarkedBy = setOf("juan")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    bookmarkedBy = setOf("juan", "123-4345-12312")
                )
            )
        )

        assertThat(
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(bookmarkedBy = "123-4345-12312")))
        )
            .containsExactlyInAnyOrder("2")
    }
}
