package com.boclips.search.service.testsupport

import com.boclips.search.service.infrastructure.ElasticSearchConfig
import com.boclips.search.service.infrastructure.ElasticSearchService.Companion.ES_INDEX
import org.apache.http.HttpHost
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.BeforeEach
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import java.util.concurrent.TimeUnit

const val port = 9350

abstract class EmbeddedElasticSearchIntegrationTest {

    companion object {
        val CONFIG = ElasticSearchConfig(
                scheme = "http",
                host = "localhost",
                port = port,
                username = "",
                password = ""
        )

        init {
            EmbeddedElastic.builder()
                    .withElasticVersion("6.3.2")
                    .withSetting(PopularProperties.HTTP_PORT, port)
                    .withStartTimeout(2, TimeUnit.MINUTES)
                    .build()
                    .start()
        }
    }

    @BeforeEach
    internal fun setUp() {
        RestHighLevelClient(RestClient.builder(HttpHost("localhost", port))).use { client ->
            if (client.indices().exists(GetIndexRequest().indices(ES_INDEX))) {
                client.indices().delete(DeleteIndexRequest(ES_INDEX))
            }
        }
    }
}
