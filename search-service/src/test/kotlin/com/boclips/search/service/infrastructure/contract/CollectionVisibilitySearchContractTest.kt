package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
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

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        visibilityForOwners = setOf(
                            VisibilityForOwner(
                                owner = null,
                                visibility = CollectionVisibilityQuery.All
                            )
                        )
                    )
                )
            ).counts.hits
        ).isEqualTo(2)
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
                        visibilityForOwners = setOf(
                            VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.privateOnly())
                        )
                    )
                )
            ).elements
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
            PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly())
                    )
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds collections matching several visibility-owner entries`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", visibility = CollectionVisibility.PUBLIC),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "seb"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "seb"
                ),
                SearchableCollectionMetadataFactory.create(id = "4", visibility = CollectionVisibility.PRIVATE)
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly()),
                        VisibilityForOwner(owner = "seb", visibility = CollectionVisibilityQuery.privateOnly())
                    )
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds collections matching several visibility-owner entries with different owners`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "jacek"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "seb"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "seb"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "4",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "jc"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "5",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "jc"
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = "jc", visibility = CollectionVisibilityQuery.All),
                        VisibilityForOwner(owner = "seb", visibility = CollectionVisibilityQuery.privateOnly())
                    )
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("3", "4", "5")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds all collections if no visibility specified`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "jacek"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "seb"
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf()
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds all private collections for any owner`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "jacek"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    visibility = CollectionVisibility.PRIVATE,
                    owner = "seb"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    visibility = CollectionVisibility.PUBLIC,
                    owner = "seb"
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.privateOnly())
                    )
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds all collections by specific IDs`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1"),
                SearchableCollectionMetadataFactory.create(id = "2", visibility = CollectionVisibility.PRIVATE),
                SearchableCollectionMetadataFactory.create(id = "3")
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    permittedIds = listOf("1", "2")
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
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
            PaginatedSearchRequest(
                query = CollectionQuery(
                    phrase = "Bear",
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly())
                    )
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
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
                    visibility = CollectionVisibility.PUBLIC,
                    subjects = listOf("Math")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    visibility = CollectionVisibility.PUBLIC,
                    subjects = listOf("Math")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    visibility = CollectionVisibility.PRIVATE,
                    subjects = listOf("Math")
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    subjectIds = listOf("Math"),
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly())
                    )
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
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
                    visibility = CollectionVisibility.PUBLIC,
                    subjects = listOf("Math")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Bear",
                    visibility = CollectionVisibility.PUBLIC,
                    subjects = listOf("Math")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Bear",
                    visibility = CollectionVisibility.PRIVATE,
                    subjects = listOf("Math")
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    phrase = "Bear",
                    subjectIds = listOf("Math"),
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly())
                    )
                )
            )
        )

        assertThat(result.elements).containsExactly("2")
    }
}
