package com.boclips.videos.service.presentation.disciplines

import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class CreateDisciplineRequest(
    @field:Size(min = 2, max = 100, message = "Discipline name must be between 1 and 100 characters")
    @field:NotNull(message = "Discipline name is required")
    var name: String?,

    @field:Size(min = 2, max = 50, message = "Discipline code must be between 1 and 50 characters")
    @field:NotNull(message = "Discipline code is required")
    @field:Pattern(message = "Discipline code must contain lower-case letter and/or hyphens only", regexp = "[a-z\\-]+")
    var code: String?
)
