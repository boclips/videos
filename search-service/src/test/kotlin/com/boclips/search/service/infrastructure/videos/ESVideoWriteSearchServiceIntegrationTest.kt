package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.rest.RestStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ESVideoWriteSearchServiceIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService
    lateinit var client: RestHighLevelClient

    @BeforeEach
    fun setUp() {
        readSearchService = ESVideoReadSearchService(CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(CONFIG.buildClient())
        client = CONFIG.buildClient()
    }

    @Test
    fun `rebuilding the index deletes previous index versions`() {
        writeSearchService.safeRebuildIndex(emptySequence())

        val previousIndices = getCurrentIndices()

        assertThat(previousIndices).isNotEmpty

        writeSearchService.safeRebuildIndex(emptySequence())

        assertThat(getCurrentIndices()).doesNotContain(*previousIndices)
    }

    @Test
    fun `rebuilding the index switches the alias to point to the new index only`() {
        writeSearchService.safeRebuildIndex(emptySequence())

        val aliasResponseOne = getAliases()

        assertThat(aliasResponseOne.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)

        writeSearchService.safeRebuildIndex(emptySequence())

        val aliasResponseTwo = getAliases()

        assertThat(aliasResponseTwo.status()).isEqualTo(RestStatus.OK)
        assertThat(aliasResponseOne.aliases.size).isEqualTo(1)
        assertThat(aliasResponseTwo.aliases.keys).doesNotContain(*aliasResponseOne.aliases.keys.toTypedArray())
    }

    @Test
    fun `calling upsert doesn't delete the index`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy")
            )
        )
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("candy"))
        )

        assertThat(results).hasSize(2)
    }

    private fun getCurrentIndices() =
        client.indices().get(GetIndexRequest().indices("video*"), RequestOptions.DEFAULT).indices

    private fun getAliases() =
        client.indices().getAlias(GetAliasesRequest(ESVideosIndex.getIndexAlias()), RequestOptions.DEFAULT)
}