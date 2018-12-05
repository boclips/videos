package com.boclips.videos.service.infrastructure.event

import java.time.ZonedDateTime

data class EventsStatus(
        val healthy: Boolean,
        val latestSearch: ZonedDateTime?,
        val latestPlaybackInSearch: ZonedDateTime?,
        val latestPlaybackStandalone: ZonedDateTime?
)