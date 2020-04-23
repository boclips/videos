package com.boclips.videos.api.request.video

import com.boclips.videos.api.common.Specifiable
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.hibernate.validator.constraints.Range

data class UpdateVideoRequest(
    val title: String? = null,
    val description: String? = null,
    val promoted: Boolean? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val subjectIds: List<String>? = null,
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    @field:Range(min = 0, max = 5, message = "Rating must be between 1 and 5")
    var rating: Int? = null,
    val tagId: String? = null,
    val attachments: Specifiable<List<AttachmentRequest>>? = null
)
