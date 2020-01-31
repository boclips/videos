package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderAgeRangeSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var collectionIndexReader: CollectionIndexReader
    private lateinit var collectionIndexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        collectionIndexReader = CollectionIndexReader(esClient)
        collectionIndexWriter = CollectionIndexWriter.createTestInstance(esClient)

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
        val collectionIds = getSearchResults(CollectionQuery(ageRangeMin = 7, ageRangeMax = 11))

        assertThat(collectionIds).hasSize(1)
        assertThat(collectionIds).doesNotContain("Pre-school")
        assertThat(collectionIds).doesNotContain("Lower-Elementary")
        assertThat(collectionIds).contains("Upper-Elementary")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Down")
        assertThat(collectionIds).doesNotContain("Middle-School")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Up")
        assertThat(collectionIds).doesNotContain("Jr-High-School")
    }

    @Test
    fun `Filter across two brackets`() {
        val collectionIds = getSearchResults(CollectionQuery(ageRangeMin = 7, ageRangeMax = 14))

        assertThat(collectionIds).hasSize(2)
        assertThat(collectionIds).doesNotContain("Pre-school")
        assertThat(collectionIds).doesNotContain("Lower-Elementary")
        assertThat(collectionIds).contains("Upper-Elementary")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Down")
        assertThat(collectionIds).contains("Middle-School")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Up")
        assertThat(collectionIds).doesNotContain("Jr-High-School")
    }

    @Test
    fun `Filtering across three brackets`() {
        val collectionIds = getSearchResults(CollectionQuery(ageRangeMin = 7, ageRangeMax = 16))

        assertThat(collectionIds).hasSize(3)
        assertThat(collectionIds).doesNotContain("Pre-school")
        assertThat(collectionIds).doesNotContain("Lower-Elementary")
        assertThat(collectionIds).contains("Upper-Elementary")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Down")
        assertThat(collectionIds).contains("Middle-School")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Up")
        assertThat(collectionIds).contains("Jr-High-School")
        assertThat(collectionIds).doesNotContain("High-School")
    }

    @Test
    fun `Filtering mid bracket only returns collections within the filter`() {
        val collectionIds = getSearchResults(CollectionQuery(ageRangeMin = 6, ageRangeMax = 16))

        assertThat(collectionIds).hasSize(3)
        assertThat(collectionIds).doesNotContain("Pre-school")
        assertThat(collectionIds).doesNotContain("Lower-Elementary")
        assertThat(collectionIds).contains("Upper-Elementary")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Down")
        assertThat(collectionIds).contains("Middle-School")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Up")
        assertThat(collectionIds).contains("Jr-High-School")
        assertThat(collectionIds).doesNotContain("High-School")
    }

    @Test
    fun `Filtering with max age range returns only collections in brackets below`() {
        val collectionIds = getSearchResults(CollectionQuery(ageRangeMax = 15))

        assertThat(collectionIds).hasSize(5)
        assertThat(collectionIds).contains("Pre-school")
        assertThat(collectionIds).contains("Lower-Elementary")
        assertThat(collectionIds).contains("Upper-Elementary")
        assertThat(collectionIds).contains("Middle-School-And-Down")
        assertThat(collectionIds).contains("Middle-School")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Up")
        assertThat(collectionIds).doesNotContain("Jr-High-School")
        assertThat(collectionIds).doesNotContain("High-School")
    }

    @Test
    fun `Filtering with lower bound returns only collections in brackets above`() {
        val collectionIds = getSearchResults(CollectionQuery(ageRangeMin = 7))

        assertThat(collectionIds).hasSize(5)
        assertThat(collectionIds).doesNotContain("Pre-school")
        assertThat(collectionIds).doesNotContain("Lower-Elementary")
        assertThat(collectionIds).contains("Upper-Elementary")
        assertThat(collectionIds).doesNotContain("Middle-School-And-Down")
        assertThat(collectionIds).contains("Middle-School")
        assertThat(collectionIds).contains("Middle-School-And-Up")
        assertThat(collectionIds).contains("Jr-High-School")
        assertThat(collectionIds).contains("High-School")
    }

    private fun getSearchResults(query: CollectionQuery) =
        collectionIndexReader.search(PaginatedSearchRequest(query = query))
}
