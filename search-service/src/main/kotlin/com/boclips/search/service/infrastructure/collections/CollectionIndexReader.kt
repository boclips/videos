package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.common.Do
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.infrastructure.common.FilterDecorator
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
import kotlin.reflect.full.createType

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
        return searchElasticSearch(query = query, startIndex = 0, windowSize = 1).totalHits?.value ?: 0L
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
            Do exhaustive when (query.sort) {
                is Sort.ByField -> {
                    val suffix = if (query.sort.fieldName.returnType == String::class.createType()) {
                        ".keyword"
                    } else {
                        ""
                    }

                    esQuery.sort(
                        query.sort.fieldName.name + suffix,
                        SortOrder.fromString(query.sort.order.toString())
                    )
                }
                is Sort.ByRandom -> TODO()
            }
        }
        return esQuery
    }

    private fun mainQuery(query: CollectionQuery): QueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                if (query.phrase.isNotEmpty()) {
                    minimumShouldMatch(1)
                    should(
                        QueryBuilders
                            .boolQuery()
                            .must(
                                QueryBuilders
                                    .matchPhraseQuery(
                                        CollectionDocument.TITLE,
                                        query.phrase
                                    )
                            )
                            .should(QueryBuilders.matchPhraseQuery(CollectionDocument.TITLE, query.phrase))
                            .boost(1.2F)
                    )
                    should(
                        QueryBuilders
                            .boolQuery()
                            .must(
                                QueryBuilders
                                    .matchPhraseQuery(
                                        CollectionDocument.DESCRIPTION,
                                        query.phrase
                                    )
                            )
                            .should(QueryBuilders.matchPhraseQuery(CollectionDocument.DESCRIPTION, query.phrase))
                    )
                            .must(QueryBuilders.wildcardQuery(CollectionDocument.DESCRIPTION, "?*"))
                }
            }
            .apply {
                filter(
                    QueryBuilders.boolQuery().apply {
                        query.visibilityForOwners.forEach { visibilityForOwner ->
                            should(
                                QueryBuilders.boolQuery().apply {
                                    visibilityForOwner.owner?.let {
                                        must(QueryBuilders.termQuery(CollectionDocument.OWNER, it))
                                    }
                                    must(
                                        QueryBuilders.termsQuery(
                                            CollectionDocument.VISIBILITY,
                                            when (visibilityForOwner.visibility) {
                                                CollectionVisibilityQuery.All -> listOf("public", "private")
                                                is CollectionVisibilityQuery.One -> when (visibilityForOwner.visibility.collectionVisibility) {
                                                    CollectionVisibility.PUBLIC -> listOf("public")
                                                    CollectionVisibility.PRIVATE -> listOf("private")
                                                }
                                            }
                                        )
                                    )
                                }
                            )
                        }

                        if (query.bookmarkedBy != null) {
                            logger.info { "Search query checking if bookmarked by: ${query.bookmarkedBy}" }
                            should(
                                QueryBuilders.termQuery(
                                    CollectionDocument.BOOKMARKED_BY,
                                    query.bookmarkedBy
                                )
                            )
                        }

                        if (query.bookmarkedBy != null && query.visibilityForOwners.any { it.owner == null }) {
                            minimumShouldMatch(2)
                        }
                    }
                )
            }
            .apply {
                if (query.subjectIds.isNotEmpty()) {
                    filter(matchSubjects(query.subjectIds))
                }
            }
            .apply {
                if (query.permittedIds != null) {
                    filter(QueryBuilders.termsQuery(CollectionDocument.ID, query.permittedIds))
                }
            }
            .apply {
                if (query.hasLessonPlans != null) {
                    filter(QueryBuilders.termsQuery(CollectionDocument.HAS_LESSON_PLANS, query.hasLessonPlans))
                }
            }
            .apply {
                FilterDecorator(this).apply(query)
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
