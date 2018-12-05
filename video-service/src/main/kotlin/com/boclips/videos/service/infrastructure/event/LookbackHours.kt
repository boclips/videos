package com.boclips.videos.service.infrastructure.event

import org.springframework.stereotype.Component

@Component
data class LookbackHours(var search: Long = 24, var playback: Long = 24)