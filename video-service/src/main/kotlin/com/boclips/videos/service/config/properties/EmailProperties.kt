package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "email")
data class EmailProperties(
    var host: String = "",
    var port: Int = 0,
    var username: String = "",
    var password: String = ""
)
