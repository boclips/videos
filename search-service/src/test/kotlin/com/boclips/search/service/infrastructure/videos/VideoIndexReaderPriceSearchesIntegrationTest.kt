package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class VideoIndexReaderPriceSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `filtering by price when there is a custom price set`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    prices = mapOf("org-id-1" to BigDecimal.valueOf(11), "DEFAULT" to BigDecimal.ONE)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    prices = mapOf("org-id-1" to BigDecimal.valueOf(12), "DEFAULT" to BigDecimal.valueOf(11))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    prices = mapOf("org-id-1" to BigDecimal.valueOf(11), "org-id-2" to BigDecimal.ONE)
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(organisationPriceFilter = "org-id-1" to setOf(BigDecimal.valueOf(11)))
                )
            )
        )

        assertThat(results.elements).hasSize(2)
        assertThat(results.elements).contains("1")
        assertThat(results.elements).contains("3")
    }

    @Test
    fun `filtering by price with no custom prices set`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    prices = mapOf("DEFAULT" to BigDecimal.valueOf(123))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    prices = mapOf("DEFAULT" to BigDecimal.valueOf(123))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    prices = mapOf("DEFAULT" to BigDecimal.valueOf(12), "org-id-2" to BigDecimal.valueOf(123))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    userQuery = UserQuery(organisationPriceFilter = "an-org-id" to setOf(BigDecimal.valueOf(123)))
                )
            )
        )

        assertThat(results.elements).hasSize(2)
        assertThat(results.elements).contains("1")
        assertThat(results.elements).contains("2")
    }
}
