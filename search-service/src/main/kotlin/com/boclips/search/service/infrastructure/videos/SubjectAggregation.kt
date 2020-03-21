package com.boclips.search.service.infrastructure.videos

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms
import org.elasticsearch.search.aggregations.bucket.terms.Terms

class SubjectAggregation {
    companion object {
        private const val SUBJECT_AGGREGATION_FILTER = "subjects"
        private const val SUBJECT_SUB_AGGREGATION = "subject ids"

        fun aggregateSubjects(aggregationFilters: BoolQueryBuilder?): FilterAggregationBuilder? {
            return AggregationBuilders
                .filter(SUBJECT_AGGREGATION_FILTER, aggregationFilters)
                .subAggregation(
                    AggregationBuilders.terms(SUBJECT_SUB_AGGREGATION).field(VideoDocument.SUBJECT_IDS)
                )
        }

        fun extractSubjectsAggregation(response: SearchResponse): MutableList<out Terms.Bucket> {
            return response
                .aggregations.get<ParsedFilter>(SUBJECT_AGGREGATION_FILTER)
                .aggregations.get<ParsedStringTerms>(SUBJECT_SUB_AGGREGATION)
                .buckets
        }
    }
}

