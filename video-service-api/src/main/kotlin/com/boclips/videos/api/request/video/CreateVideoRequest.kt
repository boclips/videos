package com.boclips.videos.api.request.video

import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreateVideoRequest(
    @field:NotBlank(message = "Provider video id is required")
    val providerVideoId: String? = null,

    @field:NotBlank(message = "Provider id is required")
    val providerId: String? = null,

    @field:NotBlank(message = "A video title is required")
    val title: String? = null,

    @field:NotBlank(message = "A video description is required")
    val description: String? = null,

    @field:NotNull(message = "Keywords are required")
    val keywords: List<String>? = null,

    @field:NotNull(message = "Released on date is required")
    val releasedOn: LocalDate? = null,

    @field:NotBlank(message = "Video type is required")
    val videoType: String? = null,

    val youtubeChannelId: String? = null,
    val legalRestrictions: String? = null,
    val playbackId: String? = null,
    val playbackProvider: String? = null,
    val analyseVideo: Boolean = true,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val subjects: Set<String>? = null
)
