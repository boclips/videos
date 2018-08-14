package com.boclips.videos.service

import com.boclips.videos.service.domain.model.Video
import org.apache.http.HttpHost
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController("v1/videos")
class VideoController {

    @Autowired
    lateinit var elasticSearchProperties: ElasticSearchProperties

    @GetMapping
    fun search(@RequestParam("query") query: String): Resources<Video> {
        val videos = getRestHighLevelClient()
                .use { client ->
                    val searchRequest = SearchRequest(arrayOf("videos"), SearchSourceBuilder()
                            .query(QueryBuilders.simpleQueryStringQuery(query)))
                    val response = client.search(searchRequest)
                    response.hits.toList().map { Video() }
                }

        return Resources(videos)
    }

    private fun getRestHighLevelClient(): RestHighLevelClient {
        val builder = RestClient.builder(HttpHost(elasticSearchProperties.host, elasticSearchProperties.port))
        return RestHighLevelClient(builder)
    }
}