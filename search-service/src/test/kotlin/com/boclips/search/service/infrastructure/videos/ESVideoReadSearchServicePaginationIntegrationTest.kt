package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

class ESVideoReadSearchServicePaginationIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `paginates search results`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
            )
        )

        val results =
            readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "banana"
                    ), startIndex = 0, windowSize = 2
                )
            )

        assertThat(results.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve any page`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
            )
        )

        val page1 = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "banana"
                ), startIndex = 0, windowSize = 2
            )
        )
        val page2 = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "banana"
                ), startIndex = 2, windowSize = 2
            )
        )
        val page3 = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "banana"
                ), startIndex = 4, windowSize = 2
            )
        )

        assertThat(page1).doesNotContainAnyElementsOf(page2)
        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(2)
        assertThat(page3).hasSize(0)
    }
}