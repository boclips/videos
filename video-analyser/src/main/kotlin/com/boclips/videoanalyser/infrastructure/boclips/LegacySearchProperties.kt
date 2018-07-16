package com.boclips.videoanalyser.infrastructure.boclips

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "legacy-search")
data class LegacySearchProperties(var baseUrl: String = "")