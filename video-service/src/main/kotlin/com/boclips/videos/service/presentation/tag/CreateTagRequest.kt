package com.boclips.videos.service.presentation.tag

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

open class CreateTagRequest(
    @field:Size(min = 2, max = 100, message = "Tag name must be between 1 and 100 characters")
    @field:NotNull(message = "Tag name is required")
    var name: String?
)