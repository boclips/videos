package com.boclips.videos.service.presentation.ageRange

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

class AgeRangeRequest(
    @field:NotNull(message = "Age range min must not be null")
    @field:Min(value = 3, message = "Age range min must be at least 3")
    @field:Max(value = 19, message = "Age range min must be less than or equal to 19")
    var min: Int,

    @field:Max(value = 19, message = "Age range max must be less than or equal to 19")
    var max: Int?
)