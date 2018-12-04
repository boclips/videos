package com.boclips.videos.service.infrastructure.video

import java.time.Duration

object DurationParser {

    fun parse(duration: String?): Duration {
        duration ?: return Duration.ZERO

        val regex = Regex("([0-9]{2}):([0-5][0-9]):([0-5][0-9])")
        val match = regex.matchEntire(duration) ?: return Duration.ZERO

        val hours = match.groups[1]!!.value.toLong()
        val minutes = match.groups[2]!!.value.toLong()
        val seconds = match.groups[3]!!.value.toLong()

        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)
    }
}
