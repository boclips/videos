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
import java.time.LocalDateTime
import java.time.ZonedDateTime

class CollectionIndexReaderSortingIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var collectionIndexReader: CollectionIndexReader
    lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `can sort by last updated date`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    updatedAt = ZonedDateTime.now()
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    updatedAt = ZonedDateTime.now().minusDays(5)
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        sort = listOf(
                            Sort.ByField(
                                CollectionMetadata::updatedAt,
                                SortOrder.DESC
                            )
                        )
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactly("100", "101")
    }

    @Test
    fun `can sort by one sort criteria`() {
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
                        sort = listOf(
                            Sort.ByField(
                                CollectionMetadata::updatedAt,
                                SortOrder.DESC
                            )
                        )
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactly("100", "101")
    }

    @Test
    fun `can sort by multiple sort criteria`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "B",
                    hasAttachments = true
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "A",
                    hasAttachments = false
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        sort = listOf(
                            Sort.ByField(
                                CollectionMetadata::hasAttachments,
                                SortOrder.DESC
                            ),
                            Sort.ByField(
                                CollectionMetadata::title,
                                SortOrder.DESC
                            )
                        )
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactly("100", "101")
    }

    @Test
    fun `can sort default collection first`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    title = "B",
                    default = true
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    title = "A",
                    default = false
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        sort = listOf(
                            Sort.ByField(
                                CollectionMetadata::default,
                                SortOrder.DESC
                            ),
                            Sort.ByField(
                                CollectionMetadata::title,
                                SortOrder.DESC
                            )
                        )
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactly("100", "101")
    }
}
