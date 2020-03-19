package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.ReindexPropertiesFactory
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
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, ReindexPropertiesFactory.create())
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
    fun `can retrieve collections by word matching on description`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    description = "Plot twist, the boy is a dog. They taught a dog to dance!"
                )
            )
        )

        val results =
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(phrase = "taught a dog")))

        Assertions.assertThat(results).containsExactly("1")
    }

    @Test
    fun `boosts title matches over description matches`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "You won't believe what this dog can do.",
                    description = "Beautiful Boy Dancing"
                ), SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Control",
                    description = "Control element"
                ), SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Beautiful Boy Dancing",
                    description = "Plot twist, the boy is a dog. They taught a dog to dance!"
                )
            )
        )

        val results =
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(phrase = "Beautiful Boy Dancing")))

        Assertions.assertThat(results).containsExactly("3", "1")
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
    fun `cannot retrieve collections with empty descriptions`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "100", title = "test", description = ""))
        )

        val results =
            collectionIndexReader.search(PaginatedSearchRequest(query = CollectionQuery(phrase = "test")))

        Assertions.assertThat(results).isEmpty()
    }

    @Test
    fun `owner and bookmark filters operate inclusively`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    owner = "teacher",
                    bookmarkedBy = emptySet()
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    owner = "stranger",
                    bookmarkedBy = setOf("teacher")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "102",
                    owner = "stranger",
                    bookmarkedBy = emptySet()
                )
            )
        )

        val results =
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        visibilityForOwners = setOf(
                            VisibilityForOwner(owner = "teacher", visibility = CollectionVisibilityQuery.All)
                        ),
                        bookmarkedBy = "teacher"
                    )
                )
            )

        Assertions.assertThat(results).hasSize(2)
        Assertions.assertThat(results).contains("100")
        Assertions.assertThat(results).contains("101")
        Assertions.assertThat(results).doesNotContain("102")
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
                                        sort = Sort.ByField(
                                                CollectionMetadata::updatedAt,
                                                SortOrder.DESC
                                        )
                                )
                        )
                )

        Assertions.assertThat(results).containsExactly("100", "101")
    }

        @Test
    fun `Returns collections by multiple word matching ignores single world matching`() {
        collectionIndexWriter.safeRebuildIndex(
                sequenceOf(
                        SearchableCollectionMetadataFactory.create(
                                id = "1",
                                title = "great war",
                                description = "that war was great"
                        ),
                        SearchableCollectionMetadataFactory.create(
                                id = "2",
                                title = "great query",
                                description = "such a great query"
                        ),
                        SearchableCollectionMetadataFactory.create(
                                id = "3",
                                title = "great gatsby",
                                description = "the great gatsby is a great film"
                        )
                )
        )

        val results =
                collectionIndexReader.search(
                        PaginatedSearchRequest(
                                query = CollectionQuery(phrase = "great gatsby"
                                        )
                                )
                        )

        Assertions.assertThat(results).containsExactly("3")
    }


}
