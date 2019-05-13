package com.boclips.videos.service.application.video.search

import com.boclips.search.service.domain.SourceType
import com.boclips.videos.service.application.video.exceptions.InvalidDateException
import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeParseException

class SearchQueryConverter {
    fun convertDuration(duration: String?): Duration? {
        return try {
            duration?.let { if (duration.isNotEmpty()) Duration.parse(it) else null }
        } catch (e: DateTimeParseException) {
            throw InvalidDurationException(duration!!)
        }
    }

    fun convertSource(source: String?): SourceType? {
        return source?.let {
            when (source) {
                "youtube" -> SourceType.YOUTUBE
                "boclips" -> SourceType.BOCLIPS
                else -> throw InvalidSourceException(source, listOf("youtube", "boclips"))
            }
        }
    }

    fun convertDate(date: String?): LocalDate? {
        return if (date.isNullOrBlank()) {
            null
        } else {
            try {
                LocalDate.parse(date)
            } catch (e: DateTimeParseException) {
                throw InvalidDateException(date)
            }
        }
    }
}