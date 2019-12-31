package com.boclips.videos.api.request.subject

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

open class CreateSubjectRequest(
    @field:Size(min = 2, max = 100, message = "Subject name must be between 1 and 100 characters")
    @field:NotNull(message = "Subject name is required")
    var name: String?
)