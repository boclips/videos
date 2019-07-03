package com.boclips.search.service.domain.videos.legacy

import java.time.Duration
import java.time.LocalDate

data class LegacyVideoMetadata(
    val id: String,
    val title: String,
    val description: String,
    val keywords: List<String>,
    val duration: Duration,
    val contentPartnerName: String,
    val contentPartnerVideoId: String,
    val releaseDate: LocalDate,
    val videoTypeTitle: String
)