package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms


fun extractStringTermBucketCounts(response: SearchResponse, filterName: String, subAggregationName: String): Set<Count> {
    return if (response.aggregations.asList().any { aggregation -> aggregation.name == filterName }) {
        response
            .aggregations.get<ParsedFilter>(filterName)
            .aggregations.get<ParsedStringTerms>(subAggregationName)
            .buckets
            .let { buckets -> parseBuckets(buckets) }
            .toSet()
    } else emptySet()
}
