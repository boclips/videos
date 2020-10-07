package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.infrastructure.IndexConfiguration
import org.elasticsearch.action.explain.ExplainRequest
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class ChannelEsQuery {
    fun mainQuery(query: ChannelQuery): QueryBuilder {
        val phrase = query.phrase

        return QueryBuilders
            .boolQuery()
            .apply {
                must(
                    QueryBuilders.boolQuery()
                        .must(
                            QueryBuilders.matchPhraseQuery(ChannelDocument.NAME, phrase)
                        )
                        .should(
                            QueryBuilders.matchPhraseQuery(IndexConfiguration.unstemmed(ChannelDocument.NAME), phrase)
                        )
                )
            }
    }
}
