package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
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
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `can retrieve collections exactly`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing"))
        )

        val results =
            collectionIndexReader.search(PaginatedIndexSearchRequest(query = CollectionQuery(phrase = "Beautiful Boy Dancing")))

        Assertions.assertThat(results.elements).containsExactly("1")
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
            collectionIndexReader.search(PaginatedIndexSearchRequest(query = CollectionQuery(phrase = "taught a dog")))

        Assertions.assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `boosts title matches over description matches`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "You won't believe what this dog can do.",
                    description = "Beautiful Boy Dancing"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Control",
                    description = "Control element"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Beautiful Boy Dancing",
                    description = "Plot twist, the boy is a dog. They taught a dog to dance!"
                )
            )
        )

        val results =
            collectionIndexReader.search(PaginatedIndexSearchRequest(query = CollectionQuery(phrase = "Beautiful Boy Dancing")))

        Assertions.assertThat(results.elements).containsExactly("3", "1")
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
            collectionIndexReader.search(PaginatedIndexSearchRequest(query = CollectionQuery(phrase = "Boy")))

        Assertions.assertThat(results.elements).containsExactly("1")
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
            collectionIndexReader.search(PaginatedIndexSearchRequest(query = CollectionQuery(phrase = "ear")))

        Assertions.assertThat(results.elements).isEmpty()
    }

    @Test
    fun `cannot retrieve collections part-word matching`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "100", title = "Beautiful Boy Dancing"))
        )

        val results =
            collectionIndexReader.search(PaginatedIndexSearchRequest(query = CollectionQuery(phrase = "Boi")))

        Assertions.assertThat(results.elements).containsExactly("100")
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
                PaginatedIndexSearchRequest(
                    query = CollectionQuery(
                        owner = "teacher",
                        searchable = null,
                        bookmarkedBy = "teacher"
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactly("100", "101")
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
                PaginatedIndexSearchRequest(
                    query = CollectionQuery(
                        phrase = "great gatsby"
                    )
                )
            )

        Assertions.assertThat(results.elements).containsExactly("3")
    }

    @Test
    fun `paginates results`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beer"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog and a gentleman"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really? I am GENTLEman"
                )
            )
        )

        val page1 =
            collectionIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    ),
                    startIndex = 0, windowSize = 2
                )
            )

        val page2 =
            collectionIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    ),
                    startIndex = 2, windowSize = 2
                )
            )

        Assertions.assertThat(page1.elements).hasSize(2)
        Assertions.assertThat(page1.counts.totalHits).isEqualTo(3)

        Assertions.assertThat(page2.elements).hasSize(1)
        Assertions.assertThat(page2.counts.totalHits).isEqualTo(3)
    }
}
