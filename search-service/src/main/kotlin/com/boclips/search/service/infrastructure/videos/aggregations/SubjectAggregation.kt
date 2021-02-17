package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder

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

        fun extractBucketCounts(response: SearchResponse): Set<Count> {
            return extractStringTermBucketCounts(
                response = response,
                filterName = SUBJECT_AGGREGATION_FILTER,
                subAggregationName = SUBJECT_SUB_AGGREGATION
            )
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

