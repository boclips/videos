package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.AGE_RANGES
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.SUBJECTS
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.allCriteria
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.removeCriteria
import com.boclips.search.service.infrastructure.videos.aggregations.AgeRangeAggregation.Companion.aggregate
import com.boclips.search.service.infrastructure.videos.aggregations.SubjectAggregation.Companion.aggregate
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.search.builder.SearchSourceBuilder

class Aggregation {
    companion object {
        fun addAggregations(builder: SearchSourceBuilder, query: VideoQuery): SearchSourceBuilder {
            return builder.apply {
                aggregation(
                    aggregate(
                        queryBuilder = removeCriteria(allCriteria(query), SUBJECTS)
                    )
                )
                aggregation(
                    aggregate(
                        queryBuilder = removeCriteria(allCriteria(query), AGE_RANGES),
                        ageRangeBuckets = query.facetDefinition?.ageRangeBuckets ?: emptyList()
                    )
                )
            }
        }

        fun extractFacetCounts(results: SearchResponse): List<FacetCount> {
            return listOf(
                FacetCount(type = FacetType.Subjects, counts = SubjectAggregation.extractBucketCounts(results)),
                FacetCount(type = FacetType.AgeRanges, counts = AgeRangeAggregation.extractBucketCounts(results))
            )
        }

        fun parseBuckets(buckets: List<MultiBucketsAggregation.Bucket>): List<Count> {
            return buckets.map { bucket -> Count(id = bucket.key.toString(), hits = bucket.docCount) }
        }
    }
}