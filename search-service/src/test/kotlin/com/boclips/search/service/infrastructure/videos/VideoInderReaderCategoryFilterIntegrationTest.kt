package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoInderReaderCategoryFilterIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `can filter by category`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "video-1", categoryCodes = listOf("A", "AB")),
                SearchableVideoMetadataFactory.create(id = "video-2", categoryCodes = listOf("C"))
            )
        )
        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    userQuery = UserQuery(categoryCodes = setOf("AB")),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactly("video-1")
    }

    @Test
    fun `can filter by multiple categories`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "video-1", categoryCodes = listOf("A", "AB")),
                SearchableVideoMetadataFactory.create(id = "video-2", categoryCodes = listOf("B", "C")),
                SearchableVideoMetadataFactory.create(id = "video-3", categoryCodes = listOf("C"))
            )
        )
        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    userQuery = UserQuery(categoryCodes = setOf("AB", "B")),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("video-1", "video-2")
    }
}
