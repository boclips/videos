package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.testsupport.CollectionSearchProvider
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.time.LocalDate

class CollectionSearchServiceContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `returns empty collection when nothing found`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    "collectionQuery that matches nothing"
                )
            )
        )

        assertThat(result.elements).hasSize(0)
        assertThat(result.counts.hits).isEqualTo(0)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `returns collections we are contracted to see even without matching visibility`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Gentleman Dancing",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    phrase = "Gentleman",
                    permittedIds = listOf("1")
                )
            )
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.counts.hits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds a collection matching title`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
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
                    title = "Who are you, really?"
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery("gentleman"))
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "3")
        assertThat(result.counts.hits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds a collection matching subjects`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", subjects = listOf("crispity", "crunchy")),
                SearchableCollectionMetadataFactory.create(id = "2", subjects = listOf("crunchy")),
                SearchableCollectionMetadataFactory.create(id = "3", subjects = emptyList())
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery(subjectIds = listOf("gentleman", "crispity")))
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1")
        assertThat(result.counts.hits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds a collection matching age range min and max`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "Pre-school", ageRangeMin = 3, ageRangeMax = 5),
                SearchableCollectionMetadataFactory.create(id = "Lower-Elementary", ageRangeMin = 5, ageRangeMax = 7)
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery(ageRangeMin = 5, ageRangeMax = 7))
        )

        assertThat(result.elements).containsExactlyInAnyOrder("Lower-Elementary")
        assertThat(result.counts.hits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds a collection matching age range`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "Pre-school", ageRangeMin = 3, ageRangeMax = 4),
                SearchableCollectionMetadataFactory.create(id = "Lower-Elementary", ageRangeMin = 5, ageRangeMax = 7)
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery(ageRanges = listOf(AgeRange(5, 7))))
        )

        assertThat(result.elements).containsExactlyInAnyOrder("Lower-Elementary")
        assertThat(result.counts.hits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `gets all collections when empty query`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableCollectionMetadataFactory.create(id = "2", title = "Beer")
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(phrase = "")
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
        assertThat(result.counts.hits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `combines own collections with bookmarked collections`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "100",
                    owner = "teacher",
                    title = "Kappa",
                    bookmarkedBy = emptySet(),
                    updatedAt = LocalDate.now().minusDays(10)
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "101",
                    owner = "stranger",
                    title = "Beta",
                    bookmarkedBy = setOf("teacher"),
                    updatedAt = LocalDate.now().minusDays(8)
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "102",
                    owner = "stranger",
                    bookmarkedBy = emptySet(),
                    updatedAt = LocalDate.now().minusDays(5)
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "103",
                    owner = "teacher",
                    title = "Alpha",
                    bookmarkedBy = emptySet(),
                    updatedAt = LocalDate.now().minusDays(1)
                )
            )
        )

        val results = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = "teacher", visibility = CollectionVisibilityQuery.All)
                    ),
                    bookmarkedBy = "teacher",
                    sort = Sort.ByField(CollectionMetadata::updatedAt, SortOrder.DESC)
                )
            )
        )

        assertThat(results.elements).hasSize(3)
        assertThat(results.elements).containsExactly("103", "101", "100")
        assertThat(results.counts.hits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `does not combine public collections with bookmarked collections, without an owner`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
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

        val results = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf(
                        VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly())
                    ),
                    bookmarkedBy = "teacher"
                )
            )
        )

        assertThat(results.elements).hasSize(1)
        assertThat(results.elements).containsExactly("101")
        assertThat(results.counts.hits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `paginates results`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
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
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    ), startIndex = 0, windowSize = 2
                )
            )
        val page2 =
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    ), startIndex = 2, windowSize = 2
                )
            )

        assertThat(page1.elements).hasSize(2)
        assertThat(page1.counts.hits).isEqualTo(3)

        assertThat(page2.elements).hasSize(1)
        assertThat(page2.counts.hits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `removed collections are not searchable`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                )
            )
        )

        writeService.removeFromSearch("1")

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    )
                )
            ).elements.isEmpty()
        )
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `can bulk remove collections from index`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(

                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "White Gentleman Dancing"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "White Gentleman Dancing"
                )
            )
        )

        writeService.bulkRemoveFromSearch(listOf("1", "2", "3"))

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    )
                )
            ).elements.isEmpty()
        )
    }
}
