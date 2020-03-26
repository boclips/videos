package com.boclips.videos.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "user.service")
class UserClientProperties(
    var baseUrl: String = "",
    var clientId: String = "",
    var clientSecret: String = "",
    var tokenUrl: String = ""
)
