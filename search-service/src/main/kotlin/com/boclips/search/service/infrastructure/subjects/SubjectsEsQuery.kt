package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class SubjectsEsQuery {
    fun mainQuery(query: SuggestionQuery<SubjectMetadata>): QueryBuilder {
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
