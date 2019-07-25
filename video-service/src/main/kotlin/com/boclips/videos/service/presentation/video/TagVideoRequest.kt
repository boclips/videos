package com.boclips.videos.service.presentation.video

import javax.validation.constraints.NotNull

open class TagVideoRequest(
    @field:NotNull(message = "Video Id is required")
    var videoId: String?,
    @field:NotNull(message = "Tag URL is required")
    var tagUrl: String?

)
