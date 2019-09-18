package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.CollectionSearchProvider
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class CollectionVisibilitySearchContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `searching without visibility set returns collections with any visibility`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
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

        assertThat(readService.count(CollectionQuery())).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds collections matching PRIVATE visibility`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
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

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    CollectionQuery(
                        visibility = CollectionVisibility.PRIVATE
                    )
                )
            )
        ).containsExactly("2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds collections matching PUBLIC visibility`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", visibility = CollectionVisibility.PUBLIC),
                SearchableCollectionMetadataFactory.create(id = "2", visibility = CollectionVisibility.PUBLIC),
                SearchableCollectionMetadataFactory.create(id = "3", visibility = CollectionVisibility.PRIVATE)
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery(visibility = CollectionVisibility.PUBLIC))
        )

        assertThat(result).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds collections by phrase respecting visibility`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Bear",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Bear",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Bear",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery(phrase = "Bear", visibility = CollectionVisibility.PUBLIC))
        )

        assertThat(result).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds collections by subject respecting visibility`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    subjects = listOf("Math"),
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    subjects = listOf("Math"),
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    subjects = listOf("Math"),
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    subjectIds = listOf("Math"),
                    visibility = CollectionVisibility.PUBLIC
                )
            )
        )

        assertThat(result).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds collections by phrase and subject respecting visibility`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Deer",
                    subjects = listOf("Math"),
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Bear",
                    subjects = listOf("Math"),
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Bear",
                    subjects = listOf("Math"),
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    phrase = "Bear",
                    subjectIds = listOf("Math"),
                    visibility = CollectionVisibility.PUBLIC
                )
            )
        )

        assertThat(result).containsExactly("2")
    }
}