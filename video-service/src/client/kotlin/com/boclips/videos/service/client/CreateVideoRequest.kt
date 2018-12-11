package com.boclips.videos.service.client

import java.time.Duration
import java.time.LocalDate

data class CreateVideoRequest(
        val provider: String,
        val providerVideoId: String,
        val title: String,
        val description: String,
        val releasedOn: LocalDate?,
        val duration: Duration?,
        val legalRestrictions: String?,
        val keywords: List<String>,
        val contentType: String,
        val playbackId: String,
        val playbackProvider: PlaybackProvider
)
