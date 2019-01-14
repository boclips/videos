package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "legacy.solr")
data class SolrProperties(
        var host: String = "",
        var port: Int = 0
)