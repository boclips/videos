package com.boclips.videos.service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "email")
data class PropertiesEmail(
        var host: String = "",
        var port: Int = 0,
        var username: String = "",
        var password: String = ""
)