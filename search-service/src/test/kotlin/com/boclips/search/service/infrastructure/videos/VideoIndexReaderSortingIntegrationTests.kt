package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class VideoIndexReaderSortingIntegrationTests : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `empty category codes are sorted first then sorted by default ingested at date`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "blue",
                    categoryCodes = listOf("A"),
                    ingestedAt = LocalDate.of(1998, 1, 1).atStartOfDay(ZoneId.systemDefault())
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "cat",
                    categoryCodes = emptyList(),
                    ingestedAt = LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.systemDefault())
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "a blue cat meows",
                    categoryCodes = listOf("B"),
                    ingestedAt = LocalDate.of(2010, 1, 1).atStartOfDay(ZoneId.systemDefault())
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "a blue cat meows",
                    categoryCodes = listOf("C"),
                    ingestedAt = LocalDate.of(1999, 1, 1).atStartOfDay(ZoneId.systemDefault())
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "a blue cat meows",
                    categoryCodes = emptyList(),
                    ingestedAt = LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault())
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoSort = Sort.ByField(VideoMetadata::categoryCodes, order = SortOrder.ASC),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.counts.totalHits).isEqualTo(5)
        Assertions.assertThat(results.elements).containsExactly("2", "5", "3", "4", "1")
    }
}
