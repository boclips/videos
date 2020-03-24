package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation

class Aggregation {
    companion object {
        fun parseBuckets(buckets: List<MultiBucketsAggregation.Bucket>): List<Count> {
            return buckets.map { bucket -> Count(id = bucket.key.toString(), hits = bucket.docCount) }
        }
    }
}