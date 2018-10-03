package com.boclips.videos.service.testsupport

import com.boclips.videos.service.config.PropertiesElasticSearch
import com.boclips.videos.service.infrastructure.search.ElasticSearchService
import com.boclips.videos.service.infrastructure.search.ElasticSearchVideo
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import java.util.concurrent.TimeUnit

@Service
@Profile("search-test")
class EmbeddedElasticSearch(
        private val propertiesElasticSearch: PropertiesElasticSearch,
        private val objectMapper: ObjectMapper
) {

    init {
        EmbeddedElastic.builder()
                .withElasticVersion("6.3.2")
                .withSetting(PopularProperties.HTTP_PORT, propertiesElasticSearch.port)
                .withStartTimeout(2, TimeUnit.MINUTES)
                .build()
                .start()

        index(
                ElasticSearchVideo(
                        id = "190",
                        referenceId = "ref-id-1",
                        title = "test title 1",
                        description = "test description 1",
                        date = "2018-02-11",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "290",
                        referenceId = "ref-id-2",
                        title = "test title 2",
                        description = "test description 2",
                        date = "2018-02-11",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "390",
                        referenceId = "ref-id-3",
                        title = "powerful video about elephants",
                        description = "test description 3",
                        date = "2018-02-11",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "490",
                        referenceId = "ref-id-4",
                        title = "clip about elephants",
                        description = "animals rock",
                        date = "2018-02-11",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "590",
                        referenceId = "ref-id-5",
                        title = "test title 5",
                        description = "test description 5",
                        date = "2018-02-11",
                        source = "cp"
                ),
                ElasticSearchVideo(
                        id = "690",
                        referenceId = "ref-id-6",
                        title = "test title 6",
                        description = "test description 6",
                        date = "2018-02-11",
                        source = "cp"
                )
        )
    }

    fun index(vararg videos: ElasticSearchVideo) {
        videos.forEach { video ->
            val document = objectMapper.writeValueAsString(video)

            RestHighLevelClient(RestClient.builder(HttpHost(propertiesElasticSearch.host, propertiesElasticSearch.port))).use { client ->
                val indexRequest = IndexRequest(ElasticSearchService.ES_INDEX, ElasticSearchService.ES_TYPE, "${video.id}")
                        .source(document, XContentType.JSON)
                        .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                client.index(indexRequest)
            }
        }
    }
}
