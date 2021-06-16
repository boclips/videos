package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelQuery
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class ChannelFilterCriteria {
    companion object {
        fun allCriteria(channelQuery: ChannelQuery): BoolQueryBuilder {
            val query = QueryBuilders.boolQuery()

            if (channelQuery.ingestTypes.isNotEmpty()) {
                query.filter(
                    QueryBuilders.termsQuery(
                        ChannelDocument.INGEST_TYPE,
                        channelQuery.ingestTypes
                    )

                )
            }

            return query
        }
    }
}
