package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderPromotedVideosIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `finds promoted videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "candy banana apple",
                    promoted = true
                )
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(userQuery = UserQuery(promoted = true), videoAccessRuleQuery = VideoAccessRuleQuery()),
                    startIndex = 0,
                    windowSize = 2
                )
            )

        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `ignores promoted flag`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "candy banana apple",
                    promoted = true
                )
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedIndexSearchRequest(
                    query = VideoQuery(
                        phrase = "banana",
                        userQuery = UserQuery(promoted = null),
                        videoAccessRuleQuery = VideoAccessRuleQuery()
                    ),
                    startIndex = 0,
                    windowSize = 2
                )
            )

        assertThat(results.elements).containsExactly("1", "2")
    }
}
