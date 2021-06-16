package com.boclips.videos.api.request.contentwarning

import javax.validation.constraints.NotNull

open class CreateContentWarningRequest(
    @field:NotNull(message = "ContentWarning label is required")
    var label: String?
)
