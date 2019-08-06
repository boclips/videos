package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import javax.validation.Valid

class UpdateCollectionRequest(
    var title: String? = null,
    var isPublic: Boolean? = null,
    var subjects: Set<String>? = null,
    var description: String? = null,

    @field:Valid
    var ageRange: AgeRangeRequest? = null
)
