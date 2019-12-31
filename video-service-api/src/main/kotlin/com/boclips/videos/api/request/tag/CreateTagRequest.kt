package com.boclips.videos.api.request.tag

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

open class CreateTagRequest(
    @field:Size(min = 2, max = 100, message = "Tag label must be between 1 and 100 characters")
    @field:NotNull(message = "Tag label is required")
    var label: String?
)
