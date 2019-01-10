package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.FIELD_DESCRIPTOR_SHINGLES
import mu.KLogging
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder

class ElasticSearchService(val config: ElasticSearchConfig) : GenericSearchService {
    companion object : KLogging() {

        const val ES_TYPE = "asset"
        const val ES_INDEX = "videos"
    }

    private val elasticSearchResultConverter = ElasticSearchResultConverter()

    private val client: RestHighLevelClient

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))

        val builder = RestClient.builder(HttpHost(config.host, config.port, config.scheme)).setHttpClientConfigCallback { httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }
        client = RestHighLevelClient(builder)
    }

    override fun search(searchRequest: PaginatedSearchRequest): List<String> {
        return searchElasticSearch(searchRequest)
                .map(elasticSearchResultConverter::convert)
                .map { it.id }
    }

    override fun count(query: Query): Long {
        return searchElasticSearch(PaginatedSearchRequest(query = query)).totalHits
    }

    private fun searchElasticSearch(searchRequest: PaginatedSearchRequest): SearchHits {
        val request = if (isIdLookup(searchRequest)) {
            buildIdLookupRequest(searchRequest)
        } else {
            buildFuzzyRequest(searchRequest)
        }
        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildFuzzyRequest(searchRequest: PaginatedSearchRequest): SearchRequest {
        val findMatchesQuery = QueryBuilders
                .multiMatchQuery(
                        searchRequest.query.phrase,
                        ElasticSearchVideo.TITLE,
                        "${ElasticSearchVideo.TITLE}.std",
                        ElasticSearchVideo.DESCRIPTION,
                        "${ElasticSearchVideo.DESCRIPTION}.std",
                        ElasticSearchVideo.CONTENT_PROVIDER,
                        ElasticSearchVideo.KEYWORDS
                )
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("75%")
                .fuzziness(Fuzziness.AUTO)

        val findExactMatchesQuery = QueryBuilders
                .multiMatchQuery(
                        searchRequest.query.phrase,
                        ElasticSearchVideo.TITLE,
                        "${ElasticSearchVideo.TITLE}.std",
                        ElasticSearchVideo.DESCRIPTION,
                        "${ElasticSearchVideo.DESCRIPTION}.std",
                        ElasticSearchVideo.CONTENT_PROVIDER,
                        ElasticSearchVideo.KEYWORDS
                )
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("75%")

        val filters =
                if (searchRequest.query.includeTags.isNotEmpty())
                    QueryBuilders.termsQuery(ElasticSearchVideo.TAGS, searchRequest.query.includeTags)
                else
                    QueryBuilders.boolQuery()

        val findMatchesQueryWithFilters = QueryBuilders.boolQuery().must(findMatchesQuery).filter(filters)
        val findExactMatchesQueryWithFilters = QueryBuilders.boolQuery().must(findExactMatchesQuery).filter(filters)

        val allMatchesQuery = QueryBuilders
                .boolQuery()
                .should(findMatchesQueryWithFilters)
                .should(findExactMatchesQueryWithFilters)
                .mustNot(QueryBuilders.termsQuery(ElasticSearchVideo.TAGS, searchRequest.query.excludeTags))

        val rescoreQuery = QueryBuilders.multiMatchQuery(searchRequest.query.phrase, "title.$FIELD_DESCRIPTOR_SHINGLES", "description.$FIELD_DESCRIPTOR_SHINGLES")
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)

        val rescorer = QueryRescorerBuilder(rescoreQuery)
                .windowSize(100)
                .setScoreMode(QueryRescoreMode.Total)

        return SearchRequest(arrayOf(ES_INDEX),
                SearchSourceBuilder()
                        .query(allMatchesQuery)
                        .from(searchRequest.startIndex)
                        .size(searchRequest.windowSize)
                        .addRescorer(rescorer))
    }

    private fun buildIdLookupRequest(searchRequest: PaginatedSearchRequest): SearchRequest {
        val findMatchesById = QueryBuilders.idsQuery().addIds(*(searchRequest.query.ids.toTypedArray()))
        val query = QueryBuilders.boolQuery().should(findMatchesById)

        return SearchRequest(arrayOf(ES_INDEX),
                SearchSourceBuilder()
                        .query(query)
                        .from(searchRequest.startIndex)
                        .size(searchRequest.windowSize))

    }

    private fun isIdLookup(searchRequest: PaginatedSearchRequest) =
            searchRequest.query.ids.isNotEmpty() == true
}
