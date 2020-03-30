package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument.Companion.DURATION_SECONDS
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.DURATION_RANGES
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.allCriteria
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.removeCriteria
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange
import java.time.Duration

class DurationAggregation {
    companion object {
        private const val DURATION_AGGREGATION_FILTER = "duration"
        private const val DURATION_SUB_AGGREGATION = "duration buckets"
        private val DEFAULT_DURATION_RANGES = listOf(
            DurationRange(Duration.ZERO, Duration.ofMinutes(2)),
            DurationRange(Duration.ofMinutes(2), Duration.ofMinutes(5)),
            DurationRange(Duration.ofMinutes(5), Duration.ofMinutes(10)),
            DurationRange(Duration.ofMinutes(10), Duration.ofMinutes(20)),
            DurationRange(Duration.ofMinutes(20), Duration.ofHours(24))
        )

        fun aggregateDuration(videoQuery: VideoQuery): FilterAggregationBuilder {
            return aggregate(
                queryBuilder = removeCriteria(queryBuilder = allCriteria(videoQuery), filterName = DURATION_RANGES),
                durationRanges = videoQuery.facetDefinition?.duration ?: emptyList()
            )
        }

        fun extractBucketCounts(response: SearchResponse): List<Count> {
            return response
                .aggregations.get<ParsedFilter>(DURATION_AGGREGATION_FILTER)
                .aggregations.get<ParsedRange>(DURATION_SUB_AGGREGATION)
                .buckets
                .let { buckets -> parseBuckets(buckets) }
        }

        private fun aggregate(
            queryBuilder: BoolQueryBuilder?,
            durationRanges: List<DurationRange>
        ): FilterAggregationBuilder {
            val durationAggregation = AggregationBuilders
                .range(DURATION_SUB_AGGREGATION)
                .field(DURATION_SECONDS)
                .apply {
                    durationRanges
                        .ifEmpty { DEFAULT_DURATION_RANGES }
                        .forEach { durationRange ->
                            addRange(
                                durationRange.toString(),
                                durationRange.min().toDouble(),
                                durationRange.max().toDouble()
                            )
                        }
                }
            return AggregationBuilders.filter(DURATION_AGGREGATION_FILTER, queryBuilder)
                .subAggregation(durationAggregation)
        }
    }
}