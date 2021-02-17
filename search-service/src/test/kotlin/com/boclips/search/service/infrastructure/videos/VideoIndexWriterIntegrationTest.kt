package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.rest.RestStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class VideoIndexWriterIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `rebuilding the index deletes previous index versions`() {
        videoIndexWriter.safeRebuildIndex(emptySequence())

        val previousIndices = getCurrentIndices()

        assertThat(previousIndices).isNotEmpty

        videoIndexWriter.safeRebuildIndex(emptySequence())

        assertThat(getCurrentIndices()).doesNotContain(*previousIndices)
    }

    @Test
    fun `rebuilding the index switches the alias to point to the new index only`() {
        videoIndexWriter.safeRebuildIndex(emptySequence())

        val aliasResponseOne = getAliases()

        assertThat(aliasResponseOne.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)

        videoIndexWriter.safeRebuildIndex(emptySequence())

        val aliasResponseTwo = getAliases()

        assertThat(aliasResponseTwo.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)
        assertThat(aliasResponseTwo.aliases.keys).doesNotContain(*aliasResponseOne.aliases.keys.toTypedArray())
    }

    @Test
    fun `calling upsert doesn't delete the index`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy", prices = mapOf("Org-id-1" to BigDecimal.valueOf(99.99), "Org-id-2" to BigDecimal.valueOf(10.99)))
            )
        )
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple")
            )
        )

        val results = videoIndexReader.search(
            PaginatedIndexSearchRequest(query = VideoQuery("candy", videoAccessRuleQuery = VideoAccessRuleQuery()))
        )

        assertThat(results.elements).hasSize(2)
    }

    private fun getCurrentIndices() =
        esClient.indices().get(GetIndexRequest("video*"), RequestOptions.DEFAULT).indices

    private fun getAliases() =
        esClient.indices().getAlias(GetAliasesRequest(VideosIndex.getIndexAlias()), RequestOptions.DEFAULT)
}
