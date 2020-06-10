package com.boclips.search.service.domain.videos.model

import java.time.LocalDate

data class VideoMetadata(
    val id: String,
    val title: String,
    val rawTitle: String,
    val description: String,
    val contentProvider: String,
    val contentPartnerId: String,
    val releaseDate: LocalDate,
    val keywords: List<String>,
    val tags: List<String>,
    val durationSeconds: Long,
    val source: SourceType,
    val transcript: String?,
    val ageRangeMin: Int?,
    val ageRangeMax: Int?,
    val types: List<VideoType>,
    val subjects: SubjectsMetadata,
    val promoted: Boolean?,
    val meanRating: Double?,
    val eligibleForStream: Boolean,
    val eligibleForDownload: Boolean,
    val attachmentTypes: Set<String>?,
    val deactivated: Boolean
)
