package com.boclips.cleanser.infrastructure.kaltura

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "kaltura")
data class KalturaProperties(var host: String = "", var session: String = "")