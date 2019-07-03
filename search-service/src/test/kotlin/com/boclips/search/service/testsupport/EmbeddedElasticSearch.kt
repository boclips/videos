package com.boclips.search.service.testsupport

import com.boclips.search.service.infrastructure.ElasticSearchClient
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import java.util.concurrent.TimeUnit

const val port = 9350

abstract class EmbeddedElasticSearchIntegrationTest {
    lateinit var esClient: RestHighLevelClient

    companion object {
        val CLIENT = ElasticSearchClient(
            scheme = "http",
            host = "localhost",
            port = port,
            username = "",
            password = ""
        )

        init {
            EmbeddedElastic.builder()
                .withElasticVersion(org.elasticsearch.Version.CURRENT.toString())
                .withSetting(PopularProperties.HTTP_PORT, port)
                .withStartTimeout(2, TimeUnit.MINUTES)
                .build()
                .start()
        }
    }

    @BeforeEach
    fun configureClient() {
        esClient = CLIENT.buildClient()
    }

    @AfterEach
    fun tearDown() {
        esClient.use { client ->
            client.indices().delete(DeleteIndexRequest("*"), RequestOptions.DEFAULT)
        }
    }
}
