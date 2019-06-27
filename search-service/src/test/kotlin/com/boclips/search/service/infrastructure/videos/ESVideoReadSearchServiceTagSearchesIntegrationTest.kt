package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

class ESVideoReadSearchServiceTagSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `all include tags must match`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("classroom")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery(includeTags = listOf("classroom", "news")))
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `all exclude tags must match`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("classroom")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    excludeTags = listOf(
                        "classroom",
                        "news"
                    )
                )
            )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `having include and exclude as the same tag returns no results`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("classroom")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    excludeTags = listOf("classroom"),
                    includeTags = listOf("classroom")
                )
            )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `searching with no filters returns news and non-news`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "banana"),
                SearchableVideoMetadataFactory.create(
                    id = "9",
                    description = "candy banana apple",
                    tags = listOf("news")
                ),
                SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery(phrase = "banana"))
        )

        assertThat(results).hasSize(3)
    }
}