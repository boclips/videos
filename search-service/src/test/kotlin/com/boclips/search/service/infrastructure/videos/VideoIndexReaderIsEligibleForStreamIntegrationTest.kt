package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderIsEligibleForStreamIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `can filter out non stream videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana",
                    eligibleForStream = true
                ), SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "banana",
                    eligibleForStream = null
                ), SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    eligibleForStream = false
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(isEligibleForStream = true))
        )

        assertThat(results.elements).containsExactly("1", "2")
    }

    @Test
    fun `does not filter if flag not specified`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana",
                    eligibleForStream = true
                ), SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    eligibleForStream = false
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(isEligibleForStream = null))
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "3")
    }
}
