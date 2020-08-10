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

class SubjectAggregation {
    companion object {
        private const val SUBJECT_AGGREGATION_FILTER = "subjects"
        private const val SUBJECT_SUB_AGGREGATION = "subject ids"

        fun aggregateSubjects(videoQuery: VideoQuery): FilterAggregationBuilder {
            return aggregate(
                queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery.userQuery),
                    VideoFilterCriteria.SUBJECTS
                )
            )
        }

        fun extractBucketCounts(response: SearchResponse): List<Count> {
            return response
                .aggregations.get<ParsedFilter>(SUBJECT_AGGREGATION_FILTER)
                .aggregations.get<ParsedStringTerms>(SUBJECT_SUB_AGGREGATION)
                .buckets
                .let { buckets -> parseBuckets(buckets) }
        }

        private fun aggregate(queryBuilder: BoolQueryBuilder?): FilterAggregationBuilder {
            return AggregationBuilders
                .filter(SUBJECT_AGGREGATION_FILTER, queryBuilder)
                .subAggregation(
                    AggregationBuilders.terms(SUBJECT_SUB_AGGREGATION).field(VideoDocument.SUBJECT_IDS).size(60)
                )
        }
    }
}

