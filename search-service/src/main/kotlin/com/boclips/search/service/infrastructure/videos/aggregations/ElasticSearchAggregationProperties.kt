package com.boclips.search.service.infrastructure.videos.aggregations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "elasticsearch.aggregation-limits")
data class ElasticSearchAggregationProperties(
    var subjects: Int = 60,
    var attachmentTypes: Int = 2,
    var videoTypes: Int = 60,
    var videoPrices: Int = 10,
    var channels: Int = 10
)
