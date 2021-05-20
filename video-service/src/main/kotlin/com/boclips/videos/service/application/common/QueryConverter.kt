package com.boclips.videos.service.application.common

import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.application.video.exceptions.InvalidDateException
import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.application.video.exceptions.InvalidTypeException
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeParseException

class QueryConverter {
    fun convertDuration(duration: String?): Duration? {
        return try {
            duration?.let { if (duration.isNotEmpty()) Duration.parse(it) else null }
        } catch (e: DateTimeParseException) {
            throw InvalidDurationException(duration!!)
        }
    }

    fun convertSource(source: String?): SourceType? {
        return source?.let {
            when (source.toLowerCase()) {
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

    fun convertTypeToVideoType(type: String): VideoType =
        when (type.toUpperCase()) {
            "NEWS" -> VideoType.NEWS
            "STOCK" -> VideoType.STOCK
            "INSTRUCTIONAL" -> VideoType.INSTRUCTIONAL
            "INSTRUCTIONAL_CLIPS" -> VideoType.INSTRUCTIONAL
            else -> throw InvalidTypeException.videoType(type, VideoType.values())
        }

    fun convertTypeToContentType(type: String): ContentType =
        when (type) {
            "NEWS" -> ContentType.NEWS
            "STOCK" -> ContentType.STOCK
            "INSTRUCTIONAL" -> ContentType.INSTRUCTIONAL
            "INSTRUCTIONAL_CLIPS" -> ContentType.INSTRUCTIONAL
            else -> throw InvalidTypeException.contentType(type, ContentType.values())
        }

    fun convertDurations(
        minDurationString: String?,
        maxDurationString: String?,
        durations: List<String>?
    ): List<DurationRange> {
        if (!durations.isNullOrEmpty()) {
            return durations.fold(mutableListOf()) { acc, durationRangeString ->
                val (min, max) = if (durationRangeString.contains('-'))
                    durationRangeString.split('-')
                else
                    listOf(durationRangeString, null)

                val minDuration = convertDuration(min)
                val maxDuration = convertDuration(max)

                if (minDuration != null || maxDuration != null) {
                    acc.add(
                        DurationRange(
                            min = minDuration ?: Duration.ofMinutes(0),
                            max = maxDuration
                        )
                    )
                }

                acc
            }
        }

        if (minDurationString != null || maxDurationString != null) {
            return listOf(
                DurationRange(
                    min = convertDuration(minDurationString) ?: Duration.ofMinutes(0),
                    max = convertDuration(maxDurationString)
                )
            )
        }

        return emptyList()
    }

    fun convertToSourceType(playbackProviderType: PlaybackProviderType) =
        when (playbackProviderType) {
            PlaybackProviderType.YOUTUBE -> SourceType.YOUTUBE
            PlaybackProviderType.KALTURA -> SourceType.BOCLIPS
        }
}
