package com.boclips.videos.api.request.video

import org.hibernate.validator.constraints.Range

data class UpdateVideoRequest(
    val title: String? = null,
    val description: String? = null,
    val promoted: Boolean? = null,
    val subjectIds: List<String>? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    @field:Range(min = 0, max = 5, message = "Rating must be between 1 and 5")
    var rating: Int? = null
)
