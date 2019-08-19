package com.boclips.videos.service.domain.model.attachment

data class Attachment(
    val attachmentId: AttachmentId,
    val type: AttachmentType,
    val description: String,
    val linkToResource: String
)
