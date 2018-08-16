package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.infrastructure.ElasticSearchProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.springframework.stereotype.Service
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import java.util.concurrent.TimeUnit

@Service
class EmbeddedElasticSearch(private val elasticSearchProperties: ElasticSearchProperties) {

    val embeddedElastic: EmbeddedElastic = EmbeddedElastic.builder()
            .withElasticVersion("6.3.2")
            .withSetting(PopularProperties.HTTP_PORT, elasticSearchProperties.port)
            .withStartTimeout(2, TimeUnit.MINUTES)
            .build()
            .start()

    init {
        index(
                Video(id = "test-id-1", title = "test title 1", description = "test description 1"),
                Video(id = "test-id-2", title = "test title 2", description = "test description 2"),
                Video(id = "test-id-3", title = "video about elephants", description = "test description 3"),
                Video(id = "test-id-4", title = "clip about elephants", description = "animals rock"),
                Video(id = "test-id-5", title = "test title 5", description = "test description 5")
        )

        Thread.sleep(4000)
    }

    fun index(vararg videos: Video) {
        val objectMapper = ObjectMapper()

        videos.forEach { video ->
            val document = objectMapper.writeValueAsString(video)

            RestHighLevelClient(RestClient.builder(HttpHost(elasticSearchProperties.host, elasticSearchProperties.port))).use { client ->
                val indexRequest = IndexRequest("videos", "_doc", "${video.id}").source(document, XContentType.JSON)
                client.index(indexRequest)
            }
        }
    }
}