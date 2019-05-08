package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import java.time.Duration
import java.time.format.DateTimeParseException

class StringToDurationConverter {
    fun convertToDuration(duration: String?): Duration? {
        return try {
            duration?.let { if (duration.isNotEmpty()) Duration.parse(it) else null}
        } catch (e: DateTimeParseException) {
            throw InvalidDurationException(duration!!)
        }
    }
}