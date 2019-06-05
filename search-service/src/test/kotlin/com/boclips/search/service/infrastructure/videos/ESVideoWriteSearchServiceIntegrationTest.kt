package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.rest.RestStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ESVideoWriteSearchServiceIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var searchSearchServiceVideo: ESVideoWriteSearchService
    lateinit var client: RestHighLevelClient

    @BeforeEach
    fun setUp() {
        searchSearchServiceVideo = ESVideoWriteSearchService(CONFIG.buildClient())
        client = CONFIG.buildClient()
    }

    @Test
    fun `rebuilding the index deletes previous index versions`() {
        searchSearchServiceVideo.safeRebuildIndex(emptySequence())

        val previousIndices = getCurrentIndices()

        assertThat(previousIndices).isNotEmpty

        searchSearchServiceVideo.safeRebuildIndex(emptySequence())

        assertThat(getCurrentIndices()).doesNotContain(*previousIndices)
    }

    @Test
    fun `rebuilding the index switches the alias to point to the new index only`() {
        searchSearchServiceVideo.safeRebuildIndex(emptySequence())

        val aliasResponseOne = getAliases()

        assertThat(aliasResponseOne.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)

        searchSearchServiceVideo.safeRebuildIndex(emptySequence())

        val aliasResponseTwo = getAliases()

        assertThat(aliasResponseTwo.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)
        assertThat(aliasResponseTwo.aliases.keys).doesNotContain(*aliasResponseOne.aliases.keys.toTypedArray())
    }

    private fun getCurrentIndices() =
        client.indices().get(GetIndexRequest().indices("video*"), RequestOptions.DEFAULT).indices

    private fun getAliases() =
        client.indices().getAlias(GetAliasesRequest(ESVideosIndex.getIndexAlias()), RequestOptions.DEFAULT)
}