package com.boclips.videos.api.request.attachments

import org.springframework.lang.NonNull
import javax.validation.constraints.NotEmpty

class AttachmentRequest(
    @NotEmpty
    @NonNull
    var linkToResource: String,

    @NonNull
    @NotEmpty
    var description: String? = null,

    @NonNull
    @NotEmpty
    var type: String
)