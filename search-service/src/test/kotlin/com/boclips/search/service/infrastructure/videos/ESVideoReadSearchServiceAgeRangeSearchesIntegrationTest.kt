package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

class ESVideoReadSearchServiceAgeRangeSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `videos within query age range`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 15
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 7
                ),
                SearchableVideoMetadataFactory.create(id = "3", title = "TED", ageRangeMin = 7),
                SearchableVideoMetadataFactory.create(id = "4", title = "TED", ageRangeMin = 3),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "TED",
                    ageRangeMin = 15,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(id = "6", title = "TED", ageRangeMin = 1, ageRangeMax = 3)
            )
        )

        val results =
            readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        ageRangeMin = 5,
                        ageRangeMax = 11
                    )
                )
            )

        assertThat(results).hasSize(4)
        assertThat(results).contains("1")
        assertThat(results).contains("2")
        assertThat(results).contains("3")
        assertThat(results).contains("4")
    }

    @Test
    fun `videos within query age range with only lower bound`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 15
                ),
                SearchableVideoMetadataFactory.create(id = "2", title = "TED", ageRangeMin = 7),
                SearchableVideoMetadataFactory.create(id = "3", title = "TED", ageRangeMin = 3),
                SearchableVideoMetadataFactory.create(id = "4", title = "TED", ageRangeMin = 1, ageRangeMax = 3)
            )
        )

        val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery(ageRangeMin = 5)))

        assertThat(results).hasSize(3)
        assertThat(results).contains("1")
        assertThat(results).contains("2")
        assertThat(results).contains("3")
    }

    @Test
    fun `videos within query age range with only upper bound`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 15
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    ageRangeMin = 7,
                    ageRangeMax = 11
                ),
                SearchableVideoMetadataFactory.create(id = "3", title = "TED", ageRangeMin = 3),
                SearchableVideoMetadataFactory.create(id = "4", title = "TED", ageRangeMin = 13),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "TED",
                    ageRangeMin = 15,
                    ageRangeMax = 18
                )
            )
        )

        val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery(ageRangeMax = 11)))

        assertThat(results).hasSize(3)
        assertThat(results).contains("1")
        assertThat(results).contains("2")
        assertThat(results).contains("3")
    }
}