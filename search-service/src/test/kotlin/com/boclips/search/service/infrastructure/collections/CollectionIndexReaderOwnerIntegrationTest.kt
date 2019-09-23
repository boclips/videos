package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
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
        collectionIndexWriter = CollectionIndexWriter(esClient)
    }

    @Test
    fun `returns public and private collections of an owner`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "juan"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "juan"
                ),
                SearchableCollectionMetadataFactory.create(id = "3", owner = "pablo")
            )
        )

        assertThat(
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        owner = "juan",
                        visibility = CollectionVisibility.ALL
                    )
                )
            )
        )
            .containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `returns no collections if it does not match the owner`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "juan"
                )
            )
        )

        assertThat(
            collectionIndexReader.search(
                PaginatedSearchRequest(query = CollectionQuery(owner = "jose"))
            )
        ).isEmpty()
    }
}
