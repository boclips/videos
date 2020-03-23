package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderPaginationIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `paginates search results`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "banana"
                    ), startIndex = 0, windowSize = 2
                )
            )

        assertThat(results.elements).hasSize(2)
        assertThat(results.counts.totalHits).isEqualTo(4)
    }

    @Test
    fun `can retrieve any page`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
            )
        )

        val page1 = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "banana"
                ), startIndex = 0, windowSize = 2
            )
        )
        val page2 = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "banana"
                ), startIndex = 2, windowSize = 2
            )
        )
        val page3 = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "banana"
                ), startIndex = 4, windowSize = 2
            )
        )

        assertThat(page1.elements).doesNotContainAnyElementsOf(page2.elements)
        assertThat(page1.elements).hasSize(2)
        assertThat(page2.elements).hasSize(2)
        assertThat(page3.elements).hasSize(0)
    }
}
