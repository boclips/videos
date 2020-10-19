package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.infrastructure.IndexConfiguration
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class ChannelEsQuery {
    fun mainQuery(query: SuggestionQuery<ChannelMetadata>): QueryBuilder {
        val phrase = query.phrase
        return QueryBuilders
            .boolQuery()
            .apply {
                ChannelsAccessRulesFilter.channelsBuildAccessRulesFilter(this, query.accessRuleQuery!!)
            }
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
