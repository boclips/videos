package com.boclips.videos.api.request.video

import org.hibernate.validator.constraints.Range
import javax.validation.constraints.NotNull

open class SetThumbnailRequest(
    @field:Range(min = 0, message = "ThumbnailSecond must be greater than 0")
    @field:NotNull(message = "Rating is required")
    var thumbnailSecond: Int?,
    var videoId: String
)
