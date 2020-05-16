package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class CollectionEsQuery {
    fun mainQuery(query: CollectionQuery): QueryBuilder {
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
    }
}
