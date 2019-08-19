package com.boclips.videos.service.infrastructure.attachment

import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType

object AttachmentDocumentConverter {
    fun convert(attachmentDocument: AttachmentDocument): Attachment = Attachment(
        attachmentId = AttachmentId(attachmentDocument.id.toHexString()),
        description = attachmentDocument.description,
        type = AttachmentType.valueOf(attachmentDocument.type),
        linkToResource = attachmentDocument.linkToResource
    )
}
