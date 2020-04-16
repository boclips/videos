package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.agerange.AgeRangeRequest
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import javax.validation.Valid

class UpdateCollectionRequest(
    var title: String? = null,
    var isPublic: Boolean? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    var subjects: Set<String>? = null,
    var description: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    var videos: List<String>? = null,
    var attachment: AttachmentRequest? = null,
    var promoted: Boolean? = null,

    @field:Valid
    var ageRange: AgeRangeRequest? = null
)

