package com.boclips.videos.api.request.video

import org.hibernate.validator.constraints.Range
import javax.validation.constraints.NotNull

sealed class SetThumbnailRequest {
    abstract val videoId: String

    data class SetThumbnailSecond(
        override var videoId: String,
        @field:NotNull(message = "thumbnailSecond is required")
        @field:Range(min = 0, message = "thumbnailSecond must be greater than 0")
        var thumbnailSecond: Int?
    ) : SetThumbnailRequest()

    data class SetCustomThumbnail(
        override var videoId: String,
        var customThumbnail: Boolean?
    ) : SetThumbnailRequest()
}
