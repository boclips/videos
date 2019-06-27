package com.boclips.videos.service.presentation.video

import org.hibernate.validator.constraints.Range
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

open class RateVideoRequest(
    @field:Range(min = 0, max = 5, message = "Rating must be between 1 and 5")
    @field:NotNull(message = "Rating is required")
    var rating: Int?,
    var videoId: String
)