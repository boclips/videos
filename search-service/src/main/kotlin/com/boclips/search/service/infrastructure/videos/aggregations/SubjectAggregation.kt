package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.infrastructure.videos.VideoDocument
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

        fun aggregateSubjects(aggregationFilters: BoolQueryBuilder?): FilterAggregationBuilder? {
            return AggregationBuilders
                .filter(SUBJECT_AGGREGATION_FILTER, aggregationFilters)
                .subAggregation(
                    AggregationBuilders.terms(SUBJECT_SUB_AGGREGATION).field(VideoDocument.SUBJECT_IDS).size(60)
                )
        }

        fun extractSubjectCounts(response: SearchResponse): List<Count> {
            return response
                .aggregations.get<ParsedFilter>(SUBJECT_AGGREGATION_FILTER)
                .aggregations.get<ParsedStringTerms>(SUBJECT_SUB_AGGREGATION)
                .buckets
                .let { buckets -> Aggregation.parseBuckets(buckets) }
        }
    }
}

