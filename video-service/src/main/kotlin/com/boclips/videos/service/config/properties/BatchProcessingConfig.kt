package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "batch-processing")
data class BatchProcessingConfig(
    var videoBatchSize: Int = 1000,
    var collectionBatchSize: Int = 1000
)
