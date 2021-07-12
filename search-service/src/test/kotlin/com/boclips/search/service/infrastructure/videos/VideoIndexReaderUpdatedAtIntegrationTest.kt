package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime

class VideoIndexReaderUpdatedAtIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `filtering by updated at when there is a match`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    updatedAt = ZonedDateTime.parse("2020-03-01T18:30Z")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    updatedAt = ZonedDateTime.parse("2020-03-01T18:30Z")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    updatedAt = ZonedDateTime.parse("2020-03-01T18:30Z")
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(updatedAtFrom = LocalDate.parse("2020-01-01"))
                )
            )
        )

        assertThat(results.elements).hasSize(2)
        assertThat(results.elements).contains("1")
        assertThat(results.elements).contains("2")
    }
}
