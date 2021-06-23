package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.infrastructure.IndexConfiguration
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class ChannelEsQuery {
    fun mainQuery(query: ChannelQuery): QueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                query.accessRuleQuery?.let {
                    ChannelsAccessRulesFilter.channelsBuildAccessRulesFilter(this, it)
                }
            }
            .apply {
                if (!query.name.isNullOrBlank()) {
                    must(QueryBuilders.termQuery(ChannelDocument.NAME, query.name))
                }
            }
            .apply {
                if (query.phrase.isNotBlank()) {
                    must(
                        QueryBuilders.boolQuery()
                            .must(
                                QueryBuilders.matchPhraseQuery(ChannelDocument.AUTOCOMPLETE_NAME, query.phrase)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(
                                    IndexConfiguration.unstemmed(ChannelDocument.AUTOCOMPLETE_NAME),
                                    query.phrase
                                )
                            )
                    )
                } else {
                    should(QueryBuilders.matchAllQuery())
                }
            }
            .apply {
                if (query.taxonomy?.categoriesWithAncestors?.isNotEmpty() == true) {
                    filter(
                        QueryBuilders.boolQuery()
                            .should(
                                QueryBuilders.termsQuery(
                                    ChannelDocument.TAXONOMY_CATEGORIES_WITH_ANCESTORS,
                                    query.taxonomy.categoriesWithAncestors.map { it.value }.toList()
                                )
                            )
                    )
                }
            }
            .apply {
                must(
                    QueryBuilders.termsQuery(
                        ChannelDocument.IS_PRIVATE,
                        false
                    )
                )
            }
    }
}
