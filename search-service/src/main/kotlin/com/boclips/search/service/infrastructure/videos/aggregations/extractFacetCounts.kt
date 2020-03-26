package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import org.elasticsearch.action.search.SearchResponse

fun extractFacetCounts(results: SearchResponse): List<FacetCount> {
    return listOf(
        FacetCount(type = FacetType.Subjects, counts = SubjectAggregation.extractBucketCounts(results)),
        FacetCount(type = FacetType.AgeRanges, counts = AgeRangeAggregation.extractBucketCounts(results))
    )
}