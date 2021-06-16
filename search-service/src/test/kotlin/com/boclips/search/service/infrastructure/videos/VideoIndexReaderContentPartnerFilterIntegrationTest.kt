package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoIndexReaderContentPartnerFilterIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `single-content-partner filter`() {
        contentPartnerTest(
            given = sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentPartnerId = "provider one"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentPartnerId = "provider two"
                )
            ),
            searchFor = VideoQuery(
                videoAccessRuleQuery = VideoAccessRuleQuery(),
                userQuery = UserQuery(
                    channelIds = setOf("provider two")
                )
            ),
            expectIds = listOf("2")
        )
    }

    @Test
    fun `multi-content-partner filter`() {
        contentPartnerTest(
            given = sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentPartnerId = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentPartnerId = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    contentPartnerId = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    contentPartnerId = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    contentPartnerId = "p3"
                )
            ),
            searchFor = VideoQuery(
                videoAccessRuleQuery = VideoAccessRuleQuery(),
                userQuery = UserQuery(
                    channelIds = setOf("p2", "p3")
                )
            ),
            expectIds = listOf("3", "4", "5")
        )
    }

    @Test
    fun `content-partner occurs before query string matching`() {
        contentPartnerTest(
            given = sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "math",
                    contentPartnerId = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "science",
                    contentPartnerId = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "math",
                    contentPartnerId = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "science",
                    contentPartnerId = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "science",
                    contentPartnerId = "p3"
                )
            ),
            searchFor = VideoQuery(
                videoAccessRuleQuery = VideoAccessRuleQuery(),
                userQuery = UserQuery(channelIds = setOf("p1")),
                phrase = "science"
            ),
            expectIds = listOf("2")
        )
    }

    private fun contentPartnerTest(given: Sequence<VideoMetadata>, searchFor: VideoQuery, expectIds: List<String>) {
        videoIndexWriter.upsert(given)

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = searchFor)
        )

        assertThat(results.elements).containsExactlyInAnyOrder(
            *expectIds.toTypedArray()
        )
    }

    @Nested
    inner class ExcludedContentPartnerIds {
        @Test
        fun `excludes content partner even when filtering by a content partner`() {
            contentPartnerTest(
                given = sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "math",
                        contentProvider = "p1",
                        contentPartnerId = "cp-id-1"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "science",
                        contentProvider = "p1",
                        contentPartnerId = "cp-id-1"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "science",
                        contentProvider = "p2",
                        contentPartnerId = "cp-id-2"
                    )
                ),
                searchFor = VideoQuery(
                    userQuery = UserQuery(channelIds = setOf("cp-id-1", "cp-id-2")),
                    videoAccessRuleQuery = VideoAccessRuleQuery(excludedContentPartnerIds = setOf("cp-id-1"))
                ),
                expectIds = listOf("3")
            )
        }

        @Test
        fun `excludes multiple content partners`() {
            contentPartnerTest(
                given = sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "math",
                        contentPartnerId = "cp-id-1"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "science",
                        contentPartnerId = "cp-id-2"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "science",
                        contentPartnerId = "cp-id-3"
                    )
                ),
                searchFor = VideoQuery(
                    phrase = "science",
                    videoAccessRuleQuery = VideoAccessRuleQuery(excludedContentPartnerIds = setOf("cp-id-1", "cp-id-3"))
                ),
                expectIds = listOf("2")
            )
        }
    }

    @Nested
    inner class IncludedChannelIds {
        @Test
        fun `filters by channel id if specified`() {
            contentPartnerTest(
                given = sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "science",
                        contentPartnerId = "cp-id-1"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "science",
                        contentPartnerId = "cp-id-2"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "science",
                        contentPartnerId = "cp-id-3"
                    )
                ),
                searchFor = VideoQuery(
                    phrase = "science",
                    videoAccessRuleQuery = VideoAccessRuleQuery(includedChannelIds = setOf("cp-id-3", "cp-id-1"))
                ),
                expectIds = listOf("1", "3")
            )
        }
    }
}
