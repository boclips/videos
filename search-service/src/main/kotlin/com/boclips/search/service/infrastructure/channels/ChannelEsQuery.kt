package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.infrastructure.IndexConfiguration
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class ChannelEsQuery {
    fun mainQuery(query: ChannelQuery): QueryBuilder {
        val phrase = query.phrase
        return QueryBuilders
            .boolQuery()
            .apply {
                query.accessRuleQuery?.let {
                    ChannelsAccessRulesFilter.channelsBuildAccessRulesFilter(this, it)
                }
            }
            .apply {
                if (phrase.isNotBlank()) {
                    must(
                        QueryBuilders.boolQuery()
                            .must(
                                QueryBuilders.matchPhraseQuery(ChannelDocument.AUTOCOMPLETE_NAME, phrase)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(
                                    IndexConfiguration.unstemmed(ChannelDocument.AUTOCOMPLETE_NAME),
                                    phrase
                                )
                            )
                    )
                } else {
                    should(QueryBuilders.matchAllQuery())
                }
            }
            .apply {
                if (query.taxonomy?.categories?.isNotEmpty() == true) {
                    filter(
                        QueryBuilders.boolQuery()
                            .must(
                                QueryBuilders.termsQuery(
                                    ChannelDocument.TAXONOMY_CATEGORIES,
                                    query.taxonomy.categories.map { it.value }.toList()
                                )
                            )
                    )
                }
            }
    }
}
