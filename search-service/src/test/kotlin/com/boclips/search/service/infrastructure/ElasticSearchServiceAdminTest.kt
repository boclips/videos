package com.boclips.search.service.infrastructure

import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.rest.RestStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ElasticSearchServiceAdminTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var searchService: ElasticSearchServiceAdmin
    lateinit var client: RestHighLevelClient

    @BeforeEach
    fun setUp() {
        val config = EmbeddedElasticSearchIntegrationTest.CONFIG
        searchService = ElasticSearchServiceAdmin(config)
        client = RestHighLevelClient(restClientBuilder(config))
    }

    @Test
    fun `rebuilding the index deletes previous index versions`() {
        searchService.safeRebuildIndex(emptySequence())

        val previousIndices = getCurrentIndices()

        assertThat(previousIndices).isNotEmpty

        searchService.safeRebuildIndex(emptySequence())

        assertThat(getCurrentIndices()).doesNotContain(*previousIndices)
    }

    @Test
    fun `rebuilding the index switches the alias to point to the new index only`() {
        searchService.safeRebuildIndex(emptySequence())

        val aliasResponseOne = getAliases()

        assertThat(aliasResponseOne.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)

        searchService.safeRebuildIndex(emptySequence())

        val aliasResponseTwo = getAliases()

        assertThat(aliasResponseTwo.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)
        assertThat(aliasResponseTwo.aliases.keys).doesNotContain(*aliasResponseOne.aliases.keys.toTypedArray())
    }

    private fun restClientBuilder(config: ElasticSearchConfig): RestClientBuilder? {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))

        return RestClient.builder(HttpHost(config.host, config.port, config.scheme))
            .setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }
    }

    private fun getCurrentIndices() =
        client.indices().get(GetIndexRequest().indices("video*"), RequestOptions.DEFAULT).indices

    private fun getAliases() =
        client.indices().getAlias(GetAliasesRequest(ElasticSearchIndex.ES_INDEX_ALIAS), RequestOptions.DEFAULT)
}