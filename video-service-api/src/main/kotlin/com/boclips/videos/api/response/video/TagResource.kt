package com.boclips.videos.api.response.video

import com.boclips.videos.api.PublicApiProjection
import com.fasterxml.jackson.annotation.JsonView

data class TagResource(
    @get:JsonView(PublicApiProjection::class)
    val label: String
)