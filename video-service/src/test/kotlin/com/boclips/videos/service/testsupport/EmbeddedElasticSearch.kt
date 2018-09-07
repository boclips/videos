package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.infrastructure.search.ElasticSearchProperties
import com.boclips.videos.service.infrastructure.search.ElasticSearchVideo
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.springframework.stereotype.Service
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import java.time.Duration
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit

@Service
class EmbeddedElasticSearch(private val elasticSearchProperties: ElasticSearchProperties) {

    init {
        EmbeddedElastic.builder()
                .withElasticVersion("6.3.2")
                .withSetting(PopularProperties.HTTP_PORT, elasticSearchProperties.port)
                .withStartTimeout(1, TimeUnit.MINUTES)
                .build()
                .start()

        index(
                ElasticSearchVideo(
                        id = "test-id-1",
                        title = "test title 1",
                        description = "test description 1",
                        date = "2018-02-11",
                        duration = "00:01:02",
                        source = "cp"
                        ),
                ElasticSearchVideo(
                        id = "test-id-2",
                        title = "test title 2",
                        description = "test description 2",
                        date = "2018-02-11",
                        duration = "00:01:02",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "test-id-3",
                        title = "powerful video about elephants",
                        description = "test description 3",
                        date = "2018-02-11",
                        duration = "00:01:02",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "test-id-4",
                        title = "clip about elephants",
                        description = "animals rock",
                        date = "2018-02-11",
                        duration = "00:01:02",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "test-id-5",
                        title = "test title 5",
                        description = "test description 5",
                        date = "2018-02-11",
                        duration = "00:01:02",
                        source = "cp"
                )
        )
    }

    fun index(vararg videos: ElasticSearchVideo) {
        val objectMapper = ObjectMapper()

        videos.forEach { video ->
            val document = objectMapper.writeValueAsString(video)

            RestHighLevelClient(RestClient.builder(HttpHost(elasticSearchProperties.host, elasticSearchProperties.port))).use { client ->
                val indexRequest = IndexRequest("videos", "_doc", "${video.id}")
                        .source(document, XContentType.JSON)
                        .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                client.index(indexRequest)
            }
        }
    }
}