package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.config.PropertiesElasticSearch
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.SearchService
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
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
        private val elasticSearchResultConverter: ElasticSearchResultConverter,
        propertiesElasticSearch: PropertiesElasticSearch
) : SearchService {
    companion object {
        const val ES_TYPE = "video"
        const val ES_INDEX = "videos"
    }

    private val client: RestHighLevelClient

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(propertiesElasticSearch.username, propertiesElasticSearch.password))

        val builder = RestClient.builder(HttpHost(propertiesElasticSearch.host, propertiesElasticSearch.port, propertiesElasticSearch.scheme)).setHttpClientConfigCallback { httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }
        client = RestHighLevelClient(builder)
    }

    override fun search(query: String): List<VideoId> {
        return searchElasticSearch(query).map { VideoId(it.id) }
    }

    private fun searchElasticSearch(query: String): List<ElasticSearchVideo> {
        val findMatchesQuery = QueryBuilders.multiMatchQuery(query, "title", "title.std", "description", "description.std", "keywords")
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("75%")
                .fuzziness(Fuzziness.AUTO)

        val rescoreQuery = QueryBuilders.multiMatchQuery(query, "title.shingles", "description.shingles")
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)

        val rescorer = QueryRescorerBuilder(rescoreQuery)
                .windowSize(100)
                .setScoreMode(QueryRescoreMode.Total)

        val searchRequest = SearchRequest(arrayOf(ES_INDEX),
                SearchSourceBuilder().query(findMatchesQuery).addRescorer(rescorer)
        )

        return client.search(searchRequest).hits.hits.map(elasticSearchResultConverter::convert)
    }

    override fun removeFromSearch(videoId: VideoId) {
        client.delete(DeleteRequest(ES_INDEX, ES_TYPE, videoId.videoId))
    }

    override fun isIndexed(videoId: VideoId): Boolean {
        val get = client.get(GetRequest(ES_INDEX, ES_TYPE, videoId.videoId))
        return get.isExists
    }

}
