package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderbestForSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `all included best for tags must match`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    tags = listOf("other", "explainer")
                )
            )
        )
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    tags = listOf("other")
                )
            )
        )
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(bestFor = listOf("other", "explainer")))
        )

        assertThat(results).containsOnly("3")
    }

    @Test
    fun `returns all documents when given null best for tags`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("other", "explainer")
                )
            )
        )
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "banana",
                    tags = listOf("hook")
                )
            )
        )
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(bestFor = null))
        )

        assertThat(results).containsExactlyInAnyOrder("1", "2", "3")
    }

    @Test
    fun `matches tags regardless of case`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    tags = listOf("other")
                )
            )
        )
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(bestFor = listOf("Other")))
        )

        assertThat(results).containsExactlyInAnyOrder("3")
    }
}
