package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionIndexReaderAgeRangeSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var collectionIndexReader: CollectionIndexReader
    private lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)

        collectionIndexWriter.upsert(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "Pre-school", ageRangeMin = 3, ageRangeMax = 5),
                SearchableCollectionMetadataFactory.create(id = "Lower-Elementary", ageRangeMin = 5, ageRangeMax = 7),
                SearchableCollectionMetadataFactory.create(id = "Upper-Elementary", ageRangeMin = 7, ageRangeMax = 11),
                SearchableCollectionMetadataFactory.create(id = "Middle-School", ageRangeMin = 11, ageRangeMax = 14),
                SearchableCollectionMetadataFactory.create(
                    id = "Middle-School-And-Up",
                    ageRangeMin = 11,
                    ageRangeMax = null
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "Middle-School-And-Down",
                    ageRangeMin = null,
                    ageRangeMax = 14
                ),
                SearchableCollectionMetadataFactory.create(id = "Jr-High-School", ageRangeMin = 14, ageRangeMax = 16),
                SearchableCollectionMetadataFactory.create(id = "High-School", ageRangeMin = 16, ageRangeMax = null)
            )
        )
    }

    @Test
    fun `Filtering across a single bracket`() {
        val results = getSearchResults(CollectionQuery(ageRangeMin = 7, ageRangeMax = 11))

        assertThat(results.elements).hasSize(1)
        assertThat(results.elements).contains("Upper-Elementary")
    }

    @Test
    fun `Filter across two brackets`() {
        val results = getSearchResults(CollectionQuery(ageRangeMin = 7, ageRangeMax = 14))

        assertThat(results.elements).hasSize(2)
        assertThat(results.elements).contains("Upper-Elementary")
        assertThat(results.elements).contains("Middle-School")
    }

    @Test
    fun `Filtering across three brackets`() {
        val results = getSearchResults(CollectionQuery(ageRangeMin = 7, ageRangeMax = 16))

        assertThat(results.elements).hasSize(3)
        assertThat(results.elements).contains("Upper-Elementary")
        assertThat(results.elements).contains("Middle-School")
        assertThat(results.elements).contains("Jr-High-School")
    }

    @Test
    fun `Filtering mid bracket only returns collections within the filter`() {
        val results = getSearchResults(CollectionQuery(ageRangeMin = 6, ageRangeMax = 16))

        assertThat(results.elements).hasSize(3)
        assertThat(results.elements).contains("Upper-Elementary")
        assertThat(results.elements).contains("Middle-School")
        assertThat(results.elements).contains("Jr-High-School")
    }

    @Test
    fun `Filtering with max age range returns only collections in brackets below`() {
        val results = getSearchResults(CollectionQuery(ageRangeMax = 15))

        assertThat(results.elements).hasSize(5)
        assertThat(results.elements).contains("Pre-school")
        assertThat(results.elements).contains("Lower-Elementary")
        assertThat(results.elements).contains("Upper-Elementary")
        assertThat(results.elements).contains("Middle-School-And-Down")
        assertThat(results.elements).contains("Middle-School")
    }

    @Test
    fun `Filtering with lower bound returns only collections in brackets above`() {
        val results = getSearchResults(CollectionQuery(ageRangeMin = 7))

        assertThat(results.elements).hasSize(5)
        assertThat(results.elements).contains("Upper-Elementary")
        assertThat(results.elements).contains("Middle-School")
        assertThat(results.elements).contains("Middle-School-And-Up")
        assertThat(results.elements).contains("Jr-High-School")
        assertThat(results.elements).contains("High-School")
    }

    @Nested
    inner class AgeRangeQueriesWithRanges {
        @Test
        fun `providing single range with min`() {
            val results = getSearchResults(CollectionQuery(ageRanges = listOf(AgeRange(min = 7))))

            assertThat(results.elements).hasSize(7)

            assertThat(results.elements).contains("Lower-Elementary")
            assertThat(results.elements).contains("Middle-School-And-Down")
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
            assertThat(results.elements).contains("Middle-School-And-Up")
            assertThat(results.elements).contains("Jr-High-School")
            assertThat(results.elements).contains("High-School")
        }

        @Test
        fun `providing single range with min and max`() {
            val results = getSearchResults(CollectionQuery(ageRanges = listOf(AgeRange(min = 7, max = 8))))

            assertThat(results.elements).hasSize(3)

            assertThat(results.elements).contains("Lower-Elementary")
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School-And-Down")
        }

        @Test
        fun `providing multiple ranges with min and max`() {
            val results = getSearchResults(
                CollectionQuery(
                    ageRanges = listOf(
                        AgeRange(min = 7, max = 8),
                        AgeRange(min = 11, max = 14)
                    )
                )
            )

            assertThat(results.elements).hasSize(6)

            assertThat(results.elements).contains("Lower-Elementary")
            assertThat(results.elements).contains("Middle-School-And-Down")
            assertThat(results.elements).contains("Upper-Elementary")
            assertThat(results.elements).contains("Middle-School")
            assertThat(results.elements).contains("Middle-School-And-Up")
            assertThat(results.elements).contains("Jr-High-School")
        }
    }

    private fun getSearchResults(query: CollectionQuery): SearchResults =
        collectionIndexReader.search(PaginatedSearchRequest(query = query))
}
