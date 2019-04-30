package com.boclips.videos.service.presentation.collections

import javax.validation.constraints.Pattern

class UpdateCollectionRequest(
    var title: String? = null,
    var isPublic: Boolean? = null,
    var subjects: Set<String>? = null,
    @field:Pattern(regexp = "[1]?[0-9]-[1]?[0-9]", message = "Invalid age range. Example: 3-5. Ranges from 0-19.")
    var ageRange: String? = null
)
