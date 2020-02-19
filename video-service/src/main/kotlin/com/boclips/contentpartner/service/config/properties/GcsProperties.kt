package com.boclips.contentpartner.service.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "gcs")
data class GcsProperties(
    var projectId: String = "",
    var secret: String = "",
    val bucketName: String = ""
    )
