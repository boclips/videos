package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.CollectionSearchProvider
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class CollectionSearchSearchabilityContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds all collections`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    searchable = null
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    searchable = false
                )
            )
        )

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        owner = null,
                        searchable = null
                    )
                )
            ).counts.totalHits
        ).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds unsearchable collections`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    searchable = null
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    searchable = false
                )
            )
        )

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        owner = null,
                        searchable = false
                    )
                )
            ).elements
        ).containsExactly("2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds searchable collections`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", searchable = true),
                SearchableCollectionMetadataFactory.create(id = "2", searchable = true),
                SearchableCollectionMetadataFactory.create(id = "3", searchable = false)
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    owner = null,
                    searchable = true
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
    }
}
