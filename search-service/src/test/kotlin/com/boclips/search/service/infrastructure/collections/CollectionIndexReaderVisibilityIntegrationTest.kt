package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderVisibilityIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter(esClient)
    }

    @Test
    fun `returns public and private collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        Assertions.assertThat(
            collectionIndexReader.count(
                CollectionQuery(
                    visibility = listOf(
                        CollectionVisibility.PRIVATE,
                        CollectionVisibility.PUBLIC
                    )
                )
            )
        ).isEqualTo(2)
    }

    @Test
    fun `returns public collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "Beautiful Dog Barking",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        visibility = listOf(
                            CollectionVisibility.PUBLIC
                        )
                    )
                )
            )

        Assertions.assertThat(results).containsExactly("100")
    }

    @Test
    fun `returns private collections`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "Beautiful Dog Barking",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(visibility = listOf(CollectionVisibility.PRIVATE))
                )
            )

        Assertions.assertThat(results).containsExactly("101")
    }

    @Test
    fun `returns public collections by default`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "Beautiful Dog Barking",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        val results = collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery()))

        Assertions.assertThat(results).containsExactly("100")
    }
}
