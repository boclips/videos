package com.boclips.videos.service.infrastructure.search

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder

class ElasticSearchService(
        private val searchHitConverter: SearchHitConverter,
        private val elasticSearchProperties: ElasticSearchProperties
) : SearchService {

    override fun search(query: String): SearchResults {
        return getRestHighLevelClient()
                .use { client ->
                    val findMatchesQuery = QueryBuilders.multiMatchQuery(query, "title", "title.std", "description", "description.std", "keywords")
                            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                            .minimumShouldMatch("75%")
                            .fuzziness(Fuzziness.AUTO)

                    val rescoreQuery = QueryBuilders.multiMatchQuery(query, "title.shingles", "description.shingles")
                            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)

                    val rescorer = QueryRescorerBuilder(rescoreQuery)
                            .windowSize(100)
                            .setScoreMode(QueryRescoreMode.Total)

                    val searchRequest = SearchRequest(arrayOf("videos"),
                            SearchSourceBuilder().query(findMatchesQuery).addRescorer(rescorer)
                    )

                    val videos = client.search(searchRequest).hits.hits.map(searchHitConverter::convert)
                    return SearchResults(videos)
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
