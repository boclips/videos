package com.boclips.search.service.domain

import java.time.LocalDate

data class VideoMetadata(
    val id: String,
    val title: String,
    val description: String,
    val contentProvider: String,
    val releaseDate: LocalDate,
    val keywords: List<String>,
    val tags: List<String>,
    val durationSeconds: Long,
    val source: String
)
