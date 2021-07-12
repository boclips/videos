package com.boclips.videos.api.request.video

import com.boclips.videos.api.request.validators.Language
import com.boclips.videos.api.request.validators.NoNullContents
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

    val description: String? = null,

    val additionalDescription: String? = null,

    @field:NotNull(message = "Keywords are required")
    @field:NoNullContents(message = "Keywords cannot contain null elements")
    val keywords: List<String>? = null,

    @field:NotNull(message = "Released on date is required")
    val releasedOn: LocalDate? = null,

    @field:NotNull(message = "Video types are required")
    @field:NoNullContents(message = "Video types cannot contain null elements")
    val videoTypes: List<String>? = null,

    val youtubeChannelId: String? = null,
    val legalRestrictions: String? = null,
    val playbackId: String? = null,
    val playbackProvider: String? = null,
    val analyseVideo: Boolean = true,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    @field:NoNullContents(message = "Subjects cannot contain null elements")
    val subjects: Set<String>? = null,
    @field:Language
    val language: String? = null,
    val isVoiced: Boolean? = null,
    val categories: List<String>? = null,
    val updatedAt: LocalDate? = null,
)
