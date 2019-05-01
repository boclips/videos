package com.boclips.videos.service.presentation.collections

import javax.validation.constraints.Pattern

class UpdateCollectionRequest(
    var title: String? = null,
    var isPublic: Boolean? = null,
    var subjects: Set<String>? = null,
    @field:Pattern(
        regexp = "$BOUNDED_RANGE|$UNBOUNDED_RANGE",
        message = "Invalid age range. Example: 3-5, or 16+. Ranges no bigger than 18."
    )
    var ageRange: String? = null
) {
    companion object {
        const val BOUNDED_RANGE = "([1]?[0-9]-[1]?[0-9])"
        const val UNBOUNDED_RANGE = "([1]?[0-9]\\+)"
    }
}
