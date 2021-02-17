package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderIsEligibleForDownloadIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `can filter out non download videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana",
                    eligibleForDownload = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    eligibleForDownload = false
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(isEligibleForDownload = true)))
        )

        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `does not filter if flag not specified`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana",
                    eligibleForDownload = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    eligibleForDownload = false
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(isEligibleForDownload = null)))
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "3")
    }
}
