package com.boclips.videoanalyser.domain.model

import java.time.LocalDateTime

data class BoclipsVideo(
        val id: Int,
        val referenceId: String,
        val title: String? = null,
        val contentProvider: String? = null,
        val contentProviderId: String? = null,
        val description: String? = null,
        val date: LocalDateTime? = null,
        val duration: String? = null
)
