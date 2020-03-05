package com.boclips.search.service.domain.videos.model

import java.time.Duration

data class DurationRange(
    val min: Duration,
    val max: Duration? = null
)
