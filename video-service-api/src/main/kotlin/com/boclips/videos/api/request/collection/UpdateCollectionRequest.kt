package com.boclips.videos.api.request.collection

import com.boclips.videos.api.request.agerange.AgeRangeRequest
import com.boclips.videos.api.request.attachments.AttachmentRequest
import javax.validation.Valid

class UpdateCollectionRequest(
    var title: String? = null,
    var isPublic: Boolean? = null,
    var subjects: Set<String>? = null,
    var description: String? = null,
    var videos: List<String>? = null,
    var attachment: AttachmentRequest? = null,
    var promoted: Boolean? = null,

    @field:Valid
    var ageRange: AgeRangeRequest? = null
)

