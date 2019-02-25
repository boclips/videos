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
import org.elasticsearch.index.query.*
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder

class ElasticSearchService(val config: ElasticSearchConfig) : GenericSearchService {
    companion object : KLogging();

    private val elasticSearchResultConverter = ElasticSearchResultConverter()

    private val client: RestHighLevelClient

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))

        val builder = RestClient.builder(HttpHost(config.host, config.port, config.scheme))
                .setHttpClientConfigCallback { httpClientBuilder ->
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
        val query = if (isIdLookup(searchRequest)) {
            buildIdLookupRequest(searchRequest.query.ids)
        } else {
            buildFuzzyRequest(searchRequest.query)
        }
        val request = SearchRequest(arrayOf(ElasticSearchIndex.ES_INDEX_ALIAS),
                query
                        .from(searchRequest.startIndex)
                        .size(searchRequest.windowSize))
        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildFuzzyRequest(query: Query): SearchSourceBuilder {
        return SearchSourceBuilder()
                .query(fuzzyQuery(query))
                .addRescorer(rescorer(query.phrase))
    }

    private fun buildIdLookupRequest(ids: List<String>): SearchSourceBuilder {
        return SearchSourceBuilder()
                .query(findMatchesById(ids))
    }

    private fun findMatchesById(ids: List<String>) = idQuery(QueryBuilders.idsQuery().addIds(*(ids.toTypedArray())))

    private fun idQuery(findMatchesById: IdsQueryBuilder?): BoolQueryBuilder {
        return QueryBuilders.boolQuery().should(findMatchesById)
    }

    private fun fuzzyQuery(query: Query): BoolQueryBuilder? {
        return QueryBuilders
                .boolQuery()
                .should(matchFieldsExceptContentPartner(query))
                .should(matchContentPartnerExactly(query).boost(1000.0F))
    }

    private fun matchContentPartnerExactly(query: Query): TermQueryBuilder {
        return QueryBuilders.termQuery(
                ElasticSearchVideo.CONTENT_PROVIDER,
                query.phrase
        )
    }

    private fun matchFieldsExceptContentPartner(query: Query): BoolQueryBuilder {
        return QueryBuilders
                .boolQuery()
                .must(matchTitleDescriptionKeyword(query.phrase))
                .should(boostTitleMatch(query.phrase))
                .should(boostDescriptionMatch(query.phrase))
                .mustNot(matchTags(query.excludeTags))
                .filter(filterByTag(query.includeTags))
    }

    private fun matchTags(excludeTags: List<String>) =
            QueryBuilders.termsQuery(ElasticSearchVideo.TAGS, excludeTags)

    private fun filterByTag(includeTags: List<String>): BoolQueryBuilder? {
        return includeTags
                .fold(QueryBuilders.boolQuery()) { acc: BoolQueryBuilder, term: String ->
                    acc.must(QueryBuilders.termQuery(ElasticSearchVideo.TAGS, term))
                }
    }

    private fun boostDescriptionMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(ElasticSearchVideo.DESCRIPTION, phrase)
    }

    private fun boostTitleMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(ElasticSearchVideo.TITLE, phrase)
    }

    private fun matchTitleDescriptionKeyword(phrase: String?): MultiMatchQueryBuilder {
        return QueryBuilders
                .multiMatchQuery(
                        phrase,
                        ElasticSearchVideo.TITLE,
                        "${ElasticSearchVideo.TITLE}.std",
                        ElasticSearchVideo.DESCRIPTION,
                        "${ElasticSearchVideo.DESCRIPTION}.std",
                        ElasticSearchVideo.KEYWORDS
                )
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("75%")
                .fuzziness(Fuzziness.ZERO)
    }

    private fun rescorer(phrase: String?): QueryRescorerBuilder {
        val rescoreQuery = QueryBuilders.multiMatchQuery(
                phrase,
                "title.$FIELD_DESCRIPTOR_SHINGLES",
                "description.$FIELD_DESCRIPTOR_SHINGLES"
        )
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
        return QueryRescorerBuilder(rescoreQuery)
                .windowSize(100)
                .setScoreMode(QueryRescoreMode.Total)
    }

    private fun isIdLookup(searchRequest: PaginatedSearchRequest) = searchRequest.query.ids.isNotEmpty()
}
