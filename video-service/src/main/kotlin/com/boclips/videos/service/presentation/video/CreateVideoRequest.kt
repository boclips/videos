package com.boclips.videos.service.presentation.video

import java.time.LocalDate

data class CreateVideoRequest(
    val provider: String? = null,
    val providerVideoId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val releasedOn: LocalDate? = null,
    val legalRestrictions: String? = null,
    val keywords: List<String>? = null,
    val videoType: String? = null,
    val playbackId: String? = null,
    val playbackProvider: String? = null,
    val subjects: Set<String>? = emptySet(),
    val searchable: Boolean? = null
)
