package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import org.elasticsearch.action.search.SearchResponse

fun extractFacetCounts(results: SearchResponse): List<FacetCount> {
    return listOf(
        FacetCount(type = FacetType.Subjects, counts = SubjectAggregation.extractBucketCounts(results)),
        FacetCount(type = FacetType.Channels, counts = ChannelAggregation.extractBucketCounts(results)),
        FacetCount(type = FacetType.AgeRanges, counts = AgeRangeAggregation.extractBucketCounts(results)),
        FacetCount(type = FacetType.Duration, counts = DurationAggregation.extractBucketCounts(results)),
        FacetCount(type = FacetType.AttachmentTypes, counts = AttachmentTypeAggregation.extractBucketCounts(results)),
        FacetCount(type = FacetType.VideoTypes, counts = VideoTypeAggregation.extractBucketCounts(results)),
        FacetCount(type = FacetType.Prices, counts = PriceAggregation.extractBucketCounts(results))
    )
}
