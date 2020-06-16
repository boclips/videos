package com.boclips.videos.service.application.attachment

import com.boclips.videos.service.domain.model.attachment.AttachmentType

class GetAttachmentTypes {
    operator fun invoke(): List<AttachmentType> = AttachmentType.values().toList()
}
