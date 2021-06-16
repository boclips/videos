package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderDeactivatedVideosIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `finds activated and deactivated videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy", deactivated = true),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple", deactivated = false)
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), userQuery = UserQuery(active = null)),
                    startIndex = 0,
                    windowSize = 2
                )
            )

        assertThat(results.elements).containsExactly("1", "2")
    }

    @Test
    fun `finds only activated videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy", deactivated = true),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple", deactivated = false)
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), userQuery = UserQuery(active = true)),
                    startIndex = 0,
                    windowSize = 2
                )
            )

        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `finds only deactivated videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy", deactivated = true),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple", deactivated = false)
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery(), userQuery = UserQuery(active = false)),
                    startIndex = 0,
                    windowSize = 2
                )
            )

        assertThat(results.elements).containsExactly("1")
    }
}
