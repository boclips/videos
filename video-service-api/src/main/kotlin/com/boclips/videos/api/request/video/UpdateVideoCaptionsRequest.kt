package com.boclips.videos.api.request.video

import javax.validation.constraints.NotNull

data class UpdateVideoCaptionsRequest(
    @field:NotNull(message = "Captions are required")
    val captions: String? = null
)
