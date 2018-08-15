package com.boclips.videos.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "elasticsearch")
data class ElasticSearchProperties(
        var scheme: String = "https",
        var host: String = "",
        var port: Int = 0,
        var username: String = "",
        var password: String = ""
)