package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderContentTypeSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `returns documents with specified content types`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "2", types = listOf(VideoType.STOCK)),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    types = listOf(VideoType.INSTRUCTIONAL, VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(id = "4", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "5", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        types = setOf(
                            VideoType.STOCK,
                            VideoType.INSTRUCTIONAL
                        )
                    )
                ),
                windowSize = 10,
                startIndex = 0
            )
        )

        assertThat(results.elements).containsOnly("2", "3", "5")
    }

    @Test
    fun `returns documents with specified content type`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "2", types = listOf(VideoType.STOCK)),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    types = listOf(VideoType.INSTRUCTIONAL, VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(id = "4", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "5", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        types = setOf(
                            VideoType.STOCK
                        )
                    )
                ),
                windowSize = 10,
                startIndex = 0
            )
        )

        assertThat(results.elements).containsOnly("2", "3", "5")
    }

    @Test
    fun `included types can be combined with phrase queries`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Wild Rhino", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Domesticated Rhino",
                    types = listOf(VideoType.INSTRUCTIONAL, VideoType.NEWS)
                ),
                SearchableVideoMetadataFactory.create(id = "3", title = "Cyborg Rhino", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                VideoQuery(
                    phrase = "Rhino",
                    userQuery = UserQuery(
                        types = setOf(
                            VideoType.NEWS
                        )
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                ),
                windowSize = 10,
                startIndex = 0
            )
        )

        assertThat(results.elements).containsOnly("1", "2")
    }

    @Test
    fun `returns all documents when given no content types to filter by`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3", types = listOf(VideoType.INSTRUCTIONAL)),
                SearchableVideoMetadataFactory.create(id = "4", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "5")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(
                        types = emptySet()
                    )
                ),
                windowSize = 10,
                startIndex = 0
            )
        )

        assertThat(results.elements).containsOnly("1", "2", "3", "4", "5")
    }

    @Test
    fun `excluded types take precedence`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "2", types = listOf(VideoType.STOCK)),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    types = listOf(VideoType.INSTRUCTIONAL, VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(id = "4", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "5", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                VideoQuery(
                    userQuery = UserQuery(
                        types = setOf(
                            VideoType.STOCK,
                            VideoType.INSTRUCTIONAL
                        )
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery(
                        excludedTypes = setOf(
                            VideoType.STOCK
                        )
                    )
                ),
                windowSize = 10,
                startIndex = 0
            )
        )

        assertThat(results.elements).hasSize(0)
    }

    @Test
    fun `excluded types can be combined with phrase queries`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Wild Rhino", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Domesticated Rhino",
                    types = listOf(VideoType.INSTRUCTIONAL, VideoType.NEWS)
                ),
                SearchableVideoMetadataFactory.create(id = "3", title = "Cyborg Rhino", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                VideoQuery(
                    phrase = "Rhino",
                    videoAccessRuleQuery = VideoAccessRuleQuery(
                        excludedTypes = setOf(
                            VideoType.NEWS
                        )
                    )
                ),
                windowSize = 10,
                startIndex = 0
            )
        )

        assertThat(results.elements).containsOnly("3")
    }
}
