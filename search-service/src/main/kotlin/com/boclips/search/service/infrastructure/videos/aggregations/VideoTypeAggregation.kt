package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder

class VideoTypeAggregation {
    companion object {
        private const val VIDEO_TYPE_AGGREGATION_FILTER = "video-types"
        private const val VIDEO_TYPE_SUB_AGGREGATION = "video type names"

        fun aggregateVideoTypes(videoQuery: VideoQuery, limit: Int): FilterAggregationBuilder {
            return aggregate(
                queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery = videoQuery.userQuery),
                    VideoFilterCriteria.VIDEO_TYPES_FILTER
                ),
                limit = limit
            )
        }

        fun extractBucketCounts(response: SearchResponse): Set<Count> {
            return extractStringTermBucketCounts(
                response = response,
                filterName = VIDEO_TYPE_AGGREGATION_FILTER,
                subAggregationName = VIDEO_TYPE_SUB_AGGREGATION
            )
        }

        private fun aggregate(queryBuilder: BoolQueryBuilder?, limit: Int): FilterAggregationBuilder {
            return AggregationBuilders
                .filter(VIDEO_TYPE_AGGREGATION_FILTER, queryBuilder)
                .subAggregation(
                    AggregationBuilders.terms(VIDEO_TYPE_SUB_AGGREGATION).field(VideoDocument.TYPES).size(limit)
                )
        }
    }
}
