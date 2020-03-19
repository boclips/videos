package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.ReindexPropertiesFactory
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
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, ReindexPropertiesFactory.create())
    }

    @Test
    fun `returns public and private collections of an owner`() {
        collectionIndexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "juan-123"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "juan-123"
                ),
                SearchableCollectionMetadataFactory.create(id = "3", owner = "pablo-123")
            )
        )

        assertThat(
            collectionIndexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        visibilityForOwners = setOf(
                            VisibilityForOwner(
                                owner = "juan-123",
                                visibility = CollectionVisibilityQuery.All
                            )
                        )
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
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        visibilityForOwners = setOf(
                            VisibilityForOwner(
                                owner = "jose",
                                visibility = CollectionVisibilityQuery.All
                            )
                        )
                    )
                )
            )
        ).isEmpty()
    }
}
