package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.infrastructure.common.HasAgeRange
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
        private val DEFAULT_AGE_RANGES = listOf(
            AgeRange(3, 5),
            AgeRange(5, 9),
            AgeRange(9, 11),
            AgeRange(11, 14),
            AgeRange(14, 16),
            AgeRange(16, 99)
        )

        fun aggregateAgeRanges(aggregationFilters: BoolQueryBuilder?): FilterAggregationBuilder? {
            val ageRangeAggregation = AggregationBuilders
                .range(AGE_RANGE_SUB_AGGREGATION)
                .apply {
                    DEFAULT_AGE_RANGES.forEach { ageRange ->
                        val inclusiveMax = ageRange.max().toDouble() + 1
                        addRange(ageRange.toString(), ageRange.min().toDouble(), inclusiveMax)
                    }
                    field(HasAgeRange.AGE_RANGE)
                }

            return AggregationBuilders
                .filter(AGE_RANGE_AGGREGATION_FILTER, aggregationFilters)
                .subAggregation(
                    ageRangeAggregation
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