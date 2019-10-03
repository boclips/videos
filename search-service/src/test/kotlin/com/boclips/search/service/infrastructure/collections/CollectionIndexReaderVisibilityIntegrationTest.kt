package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
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
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.All)
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
                        visibilityForOwners = setOf(
                            VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly())
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
                    query = CollectionQuery(
                        visibilityForOwners = setOf(
                            VisibilityForOwner(
                                owner = null,
                                visibility = CollectionVisibilityQuery.privateOnly()
                            )
                        )
                    )
                )
            )

        Assertions.assertThat(results).containsExactly("101")
    }

    @Test
    fun `returns all collections by default`() {
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

        val results = collectionIndexReader.search(
            PaginatedSearchRequest(
                query = CollectionQuery()
            )
        )

        Assertions.assertThat(results).containsExactlyInAnyOrder("100", "101")
    }
}
