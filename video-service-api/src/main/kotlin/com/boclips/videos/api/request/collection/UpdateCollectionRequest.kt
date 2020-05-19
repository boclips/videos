package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.agerange.AgeRangeRequest
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import javax.validation.Valid

class UpdateCollectionRequest(
    var title: String? = null,
    var discoverable: Boolean? = null,
    var promoted: Boolean? = null,
    var description: String? = null,
    @field:Valid
    var ageRange: AgeRangeRequest? = null,
    var attachment: AttachmentRequest? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    var subjects: Set<String>? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    var videos: List<String>? = null
)

