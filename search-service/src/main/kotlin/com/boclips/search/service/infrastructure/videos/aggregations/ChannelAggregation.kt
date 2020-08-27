package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms

class ChannelAggregation {
    companion object {
        private const val CHANNEL_AGGREGATION_FILTER = "channels"
        private const val CHANNEL_SUB_AGGREGATION = "channel ids"

        fun aggregateChannels(videoQuery: VideoQuery): FilterAggregationBuilder? {
            return aggregate(
                queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery.userQuery),
                    VideoFilterCriteria.CHANNELS
                )
            )
        }

        fun extractBucketCounts(response: SearchResponse): List<Count> {
            return if (response.aggregations.asList().any { aggregation -> aggregation.name == CHANNEL_AGGREGATION_FILTER }) {
                response
                    .aggregations.get<ParsedFilter>(CHANNEL_AGGREGATION_FILTER)
                    .aggregations.get<ParsedStringTerms>(CHANNEL_SUB_AGGREGATION)
                    .buckets
                    .let { buckets -> parseBuckets(buckets) }
            } else emptyList()
        }

        private fun aggregate(queryBuilder: BoolQueryBuilder?): FilterAggregationBuilder {
            return AggregationBuilders
                .filter(CHANNEL_AGGREGATION_FILTER, queryBuilder)
                .subAggregation(
                    AggregationBuilders.terms(CHANNEL_SUB_AGGREGATION).field(VideoDocument.CONTENT_PARTNER_ID)
                )
        }
    }
}

