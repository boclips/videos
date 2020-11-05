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

class VideoTypeAggregation {
    companion object {
        private const val VIDEO_TYPE_AGGREGATION_FILTER = "video-types"
        private const val VIDEO_TYPE_SUB_AGGREGATION = "video type names"

        fun aggregateVideoTypes(videoQuery: VideoQuery): FilterAggregationBuilder {
            return aggregate(queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery = videoQuery.userQuery),
                    VideoFilterCriteria.VIDEO_TYPES_FILTER
            ))
        }

        fun extractBucketCounts(response: SearchResponse): List<Count> {
            return response
                    .aggregations.get<ParsedFilter>(VIDEO_TYPE_AGGREGATION_FILTER)
                    .aggregations.get<ParsedStringTerms>(VIDEO_TYPE_SUB_AGGREGATION)
                    .buckets
                    .let { buckets -> parseBuckets(buckets) }
        }

        private fun aggregate(queryBuilder: BoolQueryBuilder?): FilterAggregationBuilder {
            return AggregationBuilders
                    .filter(VIDEO_TYPE_AGGREGATION_FILTER, queryBuilder)
                    .subAggregation(
                            AggregationBuilders.terms(VIDEO_TYPE_SUB_AGGREGATION).field(VideoDocument.TYPES).size(60)
                    )
        }
    }
}

