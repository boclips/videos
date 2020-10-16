package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.domain.subjects.model.SubjectQuery
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class SubjectsEsQuery {
    fun mainQuery(query: SubjectQuery): QueryBuilder {
        val phrase = query.phrase
        return QueryBuilders
            .boolQuery()
            .apply {
                must(
                    QueryBuilders.boolQuery()
                        .must(
                            QueryBuilders.matchPhraseQuery(SubjectDocument.NAME, phrase)
                        )
                )
            }
    }
}