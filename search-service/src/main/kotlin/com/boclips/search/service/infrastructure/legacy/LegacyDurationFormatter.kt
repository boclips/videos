package com.boclips.search.service.infrastructure.legacy

import java.time.Duration

object LegacyDurationFormatter {

    fun format(duration: Duration): String {
        return "${format(duration.toHours())}:${formatMod60(duration.toMinutes())}:${formatMod60(duration.seconds)}"
    }

    private fun formatMod60(value: Long): Any {
        return format(value % 60)
    }

    private fun format(value: Long): Any {
        return if (value < 10) "0$value" else value
    }
}