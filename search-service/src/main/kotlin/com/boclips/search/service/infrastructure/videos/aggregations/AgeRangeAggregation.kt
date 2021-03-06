package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.common.HasAgeRange
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
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

        fun aggregateAgeRanges(videoQuery: VideoQuery): FilterAggregationBuilder {
            return aggregate(
                queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery.userQuery),
                    VideoFilterCriteria.AGE_RANGES
                ),
                ageRangeBuckets = videoQuery.facetDefinition?.ageRangeBuckets ?: emptyList()
            )
        }

        fun extractBucketCounts(response: SearchResponse): Set<Count> {
            return response
                .aggregations.get<ParsedFilter>(AGE_RANGE_AGGREGATION_FILTER)
                .aggregations.get<ParsedRange>(AGE_RANGE_SUB_AGGREGATION)
                .buckets
                .let { buckets -> parseBuckets(buckets) }.toSet()
        }

        private fun aggregate(
            queryBuilder: BoolQueryBuilder?,
            ageRangeBuckets: List<AgeRange>
        ): FilterAggregationBuilder {
            val ageRangeAggregation = AggregationBuilders
                .range(AGE_RANGE_SUB_AGGREGATION)
                .field(HasAgeRange.AGE_RANGE)
                .apply {
                    ageRangeBuckets
                        .ifEmpty { DEFAULT_AGE_RANGES }
                        .forEach { ageRange ->
                            val inclusiveMin = ageRange.min().toDouble() + 1
                            addRange(ageRange.toString(), inclusiveMin, ageRange.max().toDouble())
                        }
                }

            return AggregationBuilders
                .filter(AGE_RANGE_AGGREGATION_FILTER, queryBuilder)
                .subAggregation(ageRangeAggregation)
        }
    }
}
