package com.boclips.videos.service.infrastructure.event.analysis

object DurationFormatter {

    fun formatSeconds(seconds: Long): String {
        val minutes = seconds / 60
        return if(minutes > 0) "${minutes}m ${seconds % 60}s" else "${seconds}s"
    }
}
