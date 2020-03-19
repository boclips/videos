package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "reindex")
data class ReindexProperties(
    var batchSize: Int = -1
)
