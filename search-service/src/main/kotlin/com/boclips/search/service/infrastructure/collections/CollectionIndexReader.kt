package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder

class CollectionIndexReader(val client: RestHighLevelClient) :
    IndexReader<CollectionMetadata, CollectionQuery> {
    companion object : KLogging();

    private val elasticSearchResultConverter = CollectionDocumentConverter()

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
            arrayOf(CollectionsIndex.getIndexAlias()),
            buildSearchRequest(query)
                .from(startIndex)
                .size(windowSize)
        )
        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildSearchRequest(query: CollectionQuery): SearchSourceBuilder {
        val esQuery = SearchSourceBuilder().query(mainQuery(query))

        if (query.sort != null) {
            esQuery.sort(query.sort.fieldName.name, SortOrder.fromString(query.sort.order.toString()))
        }

        return esQuery
    }

    private fun mainQuery(query: CollectionQuery): QueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                if (query.phrase.isNotEmpty()) {
                    must(
                        QueryBuilders
                            .boolQuery()
                            .must(
                                QueryBuilders
                                    .matchQuery(
                                        CollectionDocument.TITLE,
                                        query.phrase
                                    )
                            )
                            .should(QueryBuilders.matchPhraseQuery(CollectionDocument.TITLE, query.phrase))
                    )
                }
            }
            .apply {
                if (query.visibility.isNotEmpty()) {
                    must(
                        QueryBuilders
                            .termsQuery(
                                CollectionDocument.VISIBILITY,
                                query.visibility.map { VisibilityMapper.map(it) }
                            )
                    )
                }
            }
            .apply {
                if (query.subjectIds.isNotEmpty()) {
                    must(matchSubjects(query.subjectIds))
                }
            }
            .apply {
                if (query.owner != null) {
                    must(QueryBuilders.termQuery(CollectionDocument.OWNER, query.owner))
                }
            }
            .apply {
                if (query.bookmarkedBy != null) {
                    must(QueryBuilders.termQuery(CollectionDocument.BOOKMARKED_BY, query.bookmarkedBy))
                }
            }
            .apply {
                if (query.permittedIds != null) {
                    must(QueryBuilders.termsQuery(CollectionDocument.ID, query.permittedIds))
                }
            }
    }

    private fun matchSubjects(subjects: List<String>): BoolQueryBuilder {
        val queries = QueryBuilders.boolQuery()
        for (s: String in subjects) {
            queries.should(QueryBuilders.matchPhraseQuery(CollectionDocument.SUBJECTS, s))
        }
        return queries
    }
}
