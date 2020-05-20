package com.boclips.videos.api.request.channel

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class AgeRangeRequest(
    @field:NotBlank(message = "Id is required")
    val id: String,
    @field:NotBlank(message = "Label is required")
    val label: String? = null,
    @field:NotNull(message = "Age range lower bound is required")
    val min: Int? = null,
    val max: Int? = null
)
