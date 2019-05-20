package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.SourceType
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
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.IdsQueryBuilder
import org.elasticsearch.index.query.MatchPhraseQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder
import org.elasticsearch.search.sort.SortOrder
import java.time.Duration
import java.time.LocalDate

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
        return searchElasticSearch(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)
            .map(elasticSearchResultConverter::convert)
            .map { it.id }
    }

    override fun count(query: Query): Long {
        return searchElasticSearch(query = query, startIndex = 0, windowSize = 1).totalHits
    }

    private fun searchElasticSearch(query: Query, startIndex: Int, windowSize: Int): SearchHits {
        val esQuery = if (isIdLookup(query)) {
            buildIdLookupRequest(query.ids)
        } else {
            buildFuzzyRequest(query)
        }

        val request = SearchRequest(
            arrayOf(ElasticSearchIndex.ES_INDEX_ALIAS),
            esQuery
                .from(startIndex)
                .size(windowSize)
        )
        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildFuzzyRequest(query: Query): SearchSourceBuilder {
        val esQuery = SearchSourceBuilder()
            .query(fuzzyQuery(query))

        if (query.sort === null) {
            esQuery.addRescorer(rescorer(query.phrase))
        } else {
            esQuery.sort(query.sort.fieldName.name, SortOrder.fromString(query.sort.order.toString()))
        }

        return esQuery
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
            .should(matchContentPartnerAndTagsExactly(query).boost(1000.0F))
    }

    private fun matchContentPartnerAndTagsExactly(query: Query): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .must(
                QueryBuilders.termQuery(
                    ElasticSearchVideo.CONTENT_PROVIDER,
                    query.phrase
                )
            )
            .mustNot(matchTags(query.excludeTags))
            .filter(filterByTag(query.includeTags))
    }

    private fun matchFieldsExceptContentPartner(query: Query): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .must(matchTitleDescriptionKeyword(query.phrase))
            .apply {
                if (listOfNotNull(query.minDuration, query.maxDuration).isNotEmpty()) {
                    must(beWithinDuration(query.minDuration, query.maxDuration))
                }
            }.apply {
                if (query.source != null) {
                    filter(matchSource(query.source))
                }
            }.apply {
                if (listOfNotNull(query.releaseDateFrom, query.releaseDateTo).isNotEmpty()) {
                    must(beWithinReleaseDate(query.releaseDateFrom, query.releaseDateTo))
                }
            }
            .should(boostTitleMatch(query.phrase))
            .should(boostDescriptionMatch(query.phrase))
            .mustNot(matchTags(query.excludeTags))
            .filter(filterByTag(query.includeTags))
    }

    private fun matchSource(source: SourceType): TermQueryBuilder {
        return QueryBuilders.termQuery(
            ElasticSearchVideo.SOURCE,
            source.name.toLowerCase()
        )
    }

    private fun beWithinDuration(min: Duration?, max: Duration?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(ElasticSearchVideo.DURATION_SECONDS)

        min?.let { queryBuilder.from(it.seconds) }
        max?.let { queryBuilder.to(it.seconds) }

        return queryBuilder
    }

    private fun beWithinReleaseDate(from: LocalDate?, to: LocalDate?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(ElasticSearchVideo.RELEASE_DATE)

        from?.let { queryBuilder.from(it)}
        to?.let { queryBuilder.to(it)}

        return queryBuilder
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
                ElasticSearchVideo.TRANSCRIPT,
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

    private fun isIdLookup(query: Query) = query.ids.isNotEmpty()
}
