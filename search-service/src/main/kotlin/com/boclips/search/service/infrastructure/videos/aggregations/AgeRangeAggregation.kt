package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.aggregations.Aggregation.Companion.parseBuckets
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange

class AgeRangeAggregation {
    companion object {
        private const val AGE_RANGE_AGGREGATION_FILTER = "ageRanges"
        private const val AGE_RANGE_SUB_AGGREGATION = "ageRange buckets"

        fun aggregateAgeRanges(aggregationFilters: BoolQueryBuilder?): FilterAggregationBuilder? {
            return AggregationBuilders
                .filter(AGE_RANGE_AGGREGATION_FILTER, aggregationFilters)
                .subAggregation(
                    AggregationBuilders
                        .range(AGE_RANGE_SUB_AGGREGATION)
                        .addRange("3-5", 3.0, 5.0)
                        .addRange("5-9", 5.0, 9.0)
                        .addRange("9-11", 9.0, 11.0)
                        .addRange("11-14", 11.0, 14.0)
                        .addRange("14-16", 14.0, 16.0)
                        .addUnboundedFrom("16+", 16.0)
                        .field(VideoDocument.AGE_RANGE)
                )
        }

        fun extractAgeRangeCounts(response: SearchResponse): List<Count> {
            return response
                .aggregations.get<ParsedFilter>(AGE_RANGE_AGGREGATION_FILTER)
                .aggregations.get<ParsedRange>(AGE_RANGE_SUB_AGGREGATION)
                .buckets
                .let { buckets -> parseBuckets(buckets) }
        }
    }
}
