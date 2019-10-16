package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderTagSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `all include tags must match`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("classroom")
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(includeTags = listOf("classroom", "news")))
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `all exclude tags must match`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("classroom")
                )
            )
        )

        val results = videoIndexReader.search(
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
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("classroom")
                )
            )
        )

        val results = videoIndexReader.search(
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
        videoIndexWriter.upsert(
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

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(phrase = "banana"))
        )

        assertThat(results).hasSize(3)
    }
}
