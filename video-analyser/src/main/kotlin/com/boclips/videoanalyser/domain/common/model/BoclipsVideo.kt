package com.boclips.videoanalyser.domain.common.model

import java.time.LocalDateTime

data class BoclipsVideo(
        val id: String,
        val referenceId: String? = null,
        val title: String? = null,
        val contentProvider: String? = null,
        val contentProviderId: String? = null,
        val description: String? = null,
        val date: LocalDateTime? = null,
        val duration: String? = null
) {
    fun kalturaReferenceId() = if (referenceId.isNullOrBlank()) id else referenceId!!
}
