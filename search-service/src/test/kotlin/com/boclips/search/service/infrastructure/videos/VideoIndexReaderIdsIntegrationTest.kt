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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VideoIndexReaderIdsIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient, ElasticSearchAggregationProperties())
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Nested
    inner class PermittedIds {
        @Test
        fun `limits search when permitted ids are specified`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
                )
            )

            val results = videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(
                            permittedVideoIds = setOf("1")
                        ),
                        phrase = "apple"
                    )
                )
            )

            assertThat(results.elements).containsExactly("1")
        }

        @Test
        fun `other filters still work when limiting search`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "apple", types = listOf(VideoType.NEWS)),
                    SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas"),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "golf is cool",
                        types = listOf(VideoType.NEWS)
                    )
                )
            )

            val results = videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(permittedVideoIds = setOf("1", "2")),
                        phrase = "apple",
                        userQuery = UserQuery(types = setOf(VideoType.NEWS))
                    )
                )
            )

            assertThat(results.elements).containsExactly("1")
        }

        @Test
        fun `does not limit search when permitted ids is null`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
                )
            )

            val results = videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(
                            permittedVideoIds = null
                        ),
                        phrase = "apple"
                    )
                )
            )

            assertThat(results.elements).containsExactlyInAnyOrder("1", "2")
        }

        @Test
        fun `does not limit search when permitted ids is empty`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
                )
            )

            val results = videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(
                            permittedVideoIds = emptySet()
                        ),
                        phrase = "apple"
                    )
                )
            )

            assertThat(results.elements).containsExactlyInAnyOrder("1", "2")
        }
    }

    @Nested
    inner class DeniedIds {
        @Test
        fun `denied videos are omitted by search`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
                )
            )

            val results = videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(deniedVideoIds = setOf("1")),
                        phrase = "apple"
                    )
                )
            )

            assertThat(results.elements).containsExactly("2")
        }

        @Test
        fun `denied videos are omitted by search, and override permitted ids`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas"),
                    SearchableVideoMetadataFactory.create(id = "3", title = "apple & bananas & pineapples")
                )
            )

            val results = videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(
                            deniedVideoIds = setOf("1"),
                            permittedVideoIds = setOf("1", "2")
                        ),
                        phrase = "apple"
                    )
                )
            )

            assertThat(results.elements).containsExactly("2")
        }

        @Test
        fun `denied videos not matching any ids are ignored`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                    SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
                )
            )

            val results = videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        videoAccessRuleQuery = VideoAccessRuleQuery(deniedVideoIds = setOf("100")),
                        phrase = "apple"
                    )
                )
            )

            assertThat(results.elements).containsExactly("1", "2")
        }
    }
}
