package com.boclips.videos.service.infrastructure

import com.boclips.videos.service.domain.SearchService
import com.boclips.videos.service.domain.model.Video
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Service

@Service
class ElasticSearchService(private val elasticSearchProperties: ElasticSearchProperties) : SearchService {

    override fun search(query: String): List<Video> {
        return getRestHighLevelClient()
                .use { client ->
                    val searchRequest = SearchRequest(arrayOf("videos"), SearchSourceBuilder()
                            .query(QueryBuilders.simpleQueryStringQuery(query)))
                    val response = client.search(searchRequest)
                    response.hits.hits.map { it.sourceAsMap }.map { result -> Video(title = result["title"].toString()) }
                }
    }

    private fun getRestHighLevelClient(): RestHighLevelClient {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(elasticSearchProperties.username, elasticSearchProperties.password))

        val builder = RestClient.builder(HttpHost(elasticSearchProperties.host, elasticSearchProperties.port, elasticSearchProperties.scheme)).setHttpClientConfigCallback { httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }
        return RestHighLevelClient(builder)
    }
}
