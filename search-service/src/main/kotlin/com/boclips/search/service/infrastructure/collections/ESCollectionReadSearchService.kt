package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.FIELD_DESCRIPTOR_SHINGLES
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.MatchPhraseQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder
import org.elasticsearch.search.sort.SortOrder

class ESCollectionReadSearchService(val client: RestHighLevelClient) :
    ReadSearchService<CollectionMetadata, CollectionQuery> {
    companion object : KLogging();

    private val elasticSearchResultConverter =
        ESCollectionConverter()

    override fun search(searchRequest: PaginatedSearchRequest<CollectionQuery>): List<String> {
        return searchElasticSearch(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)
            .map(elasticSearchResultConverter::convert)
            .map { it.id }
    }

    override fun count(query: CollectionQuery): Long {
        return searchElasticSearch(query = query, startIndex = 0, windowSize = 1).totalHits
    }

    private fun searchElasticSearch(query: CollectionQuery, startIndex: Int, windowSize: Int): SearchHits {
        val request = SearchRequest(
            arrayOf(ESCollectionsIndex.getIndexAlias()),
            buildFuzzyRequest(query)
                .from(startIndex)
                .size(windowSize)
        )
        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildFuzzyRequest(query: CollectionQuery): SearchSourceBuilder {
        val esQuery = SearchSourceBuilder().query(fuzzyQuery(query))

        if (!query.phrase.isNullOrBlank()) {
            esQuery.addRescorer(rescorer(query.phrase))
        } else if (query.sort != null) {
            esQuery.sort(query.sort.fieldName.name, SortOrder.fromString(query.sort.order.toString()))
        }

        return esQuery
    }

    private fun fuzzyQuery(query: CollectionQuery): QueryBuilder {
        return if (query.phrase.isNullOrEmpty()) {
            QueryBuilders.matchAllQuery()
        } else {
            QueryBuilders
                .boolQuery()
                .should(matchTitle(query))
        }
    }

    private fun matchTitle(query: CollectionQuery): QueryBuilder {
        return QueryBuilders
            .boolQuery()
            .must(matchTitle(query.phrase))
            .should(boostTitleMatch(query.phrase))
    }

    private fun boostTitleMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(ESCollection.TITLE, phrase)
    }

    private fun matchTitle(phrase: String?): MultiMatchQueryBuilder {
        return QueryBuilders
            .multiMatchQuery(
                phrase,
                ESCollection.TITLE,
                "${ESCollection.TITLE}.std"
            )
            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
            .fuzziness(Fuzziness.ONE)
    }

    private fun rescorer(phrase: String?): QueryRescorerBuilder {

        val rescoreQuery = QueryBuilders.multiMatchQuery(
            phrase,
            "title.$FIELD_DESCRIPTOR_SHINGLES"
        )
            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
        return QueryRescorerBuilder(rescoreQuery)
            .windowSize(100)
            .setScoreMode(QueryRescoreMode.Total)
    }
}
