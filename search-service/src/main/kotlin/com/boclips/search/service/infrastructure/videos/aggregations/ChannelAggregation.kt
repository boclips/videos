package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder

class ChannelAggregation {
    companion object {
        private const val CHANNEL_AGGREGATION_FILTER = "channels"
        private const val SELECTED_CHANNEL_AGGREGATION_FILTER = "selected-channels"
        private const val CHANNEL_SUB_AGGREGATION = "channel ids"

        fun aggregateChannels(videoQuery: VideoQuery): FilterAggregationBuilder? {
            return aggregate(
                queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery.userQuery),
                    VideoFilterCriteria.CHANNEL_IDS_FILTER
                ),
                filterName = CHANNEL_AGGREGATION_FILTER
            )
        }

        fun aggregateSelectedChannels(videoQuery: VideoQuery): FilterAggregationBuilder? {
            return aggregate(
                queryBuilder = VideoFilterCriteria.allCriteria(videoQuery.userQuery),
                filterName = SELECTED_CHANNEL_AGGREGATION_FILTER
            )
        }

        fun extractBucketCounts(response: SearchResponse): Set<Count> {
            val allChannels =
                extractStringTermBucketCounts(response, CHANNEL_AGGREGATION_FILTER, CHANNEL_SUB_AGGREGATION)
            val selectedChannels =
                extractStringTermBucketCounts(response, SELECTED_CHANNEL_AGGREGATION_FILTER, CHANNEL_SUB_AGGREGATION)

            return allChannels + selectedChannels
        }

        private fun aggregate(queryBuilder: BoolQueryBuilder?, filterName: String): FilterAggregationBuilder {
            return AggregationBuilders
                .filter(filterName, queryBuilder)
                .subAggregation(
                    AggregationBuilders.terms(CHANNEL_SUB_AGGREGATION).field(VideoDocument.CONTENT_PARTNER_ID)
                        .size(1000)
                )
        }
    }
}
