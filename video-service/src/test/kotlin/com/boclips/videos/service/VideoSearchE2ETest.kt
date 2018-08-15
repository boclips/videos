package com.boclips.videos.service

import com.boclips.videos.service.domain.model.Video
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.IndexSettings
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import java.util.concurrent.TimeUnit

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VideoSearchE2ETest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var elasticSearchProperties: ElasticSearchProperties

    lateinit var embeddedElastic: EmbeddedElastic

    @Before
    fun setUp() {
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("6.3.2")
                .withSetting(PopularProperties.HTTP_PORT, elasticSearchProperties.port)
                .withIndex("videos", IndexSettings.builder().build())
                .withStartTimeout(2, TimeUnit.MINUTES)
                .build()
                .start()
    }

    @Test
    fun `exposes search endpoint`() {
        indexVideos(
                Video(id = "test-id-1", title = "test title 1", description = "test description 1"),
                Video(id = "test-id-2", title = "test title 2", description = "test description 2"),
                Video(id = "test-id-3", title = "video about elephants", description = "test description 3"),
                Video(id = "test-id-4", title = "clip about elephants", description = "animals rock"),
                Video(id = "test-id-5", title = "test title 5", description = "test description 5")
        )

        Thread.sleep(4000)

        mockMvc.perform(get("/v1/videos?query=elephants"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videoList", hasSize<Any>(2)))
                .andExpect(jsonPath("$._embedded.videoList[0].title", containsString("elephants")))
                .andExpect(jsonPath("$._embedded.videoList[1].title", containsString("elephants")))
    }

    fun indexVideos(vararg videos: Video) {
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
