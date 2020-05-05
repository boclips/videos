package com.boclips.videos.api.response.video

import com.boclips.videos.api.BoclipsInternalProjection
import com.fasterxml.jackson.annotation.JsonView

data class CaptionsResource (
    @get:JsonView(BoclipsInternalProjection::class)
    val content: String
)