package com.boclips.videos.api.request.video

import javax.validation.constraints.NotNull

data class UpdateVideoCaptionsRequest (
    @field:NotNull(message = "Transcript is required")
    val transcript: String? = null
)