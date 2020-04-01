package com.boclips.contentpartner.service.presentation.converters

import com.boclips.videos.service.application.video.exceptions.InvalidDateException
import java.time.LocalDate
import java.time.format.DateTimeParseException

object DateConverter {
    fun convert(s: String): LocalDate =
        try {
            LocalDate.parse(s)
        } catch (e: DateTimeParseException) {
            throw InvalidDateException(s)
        }
}
