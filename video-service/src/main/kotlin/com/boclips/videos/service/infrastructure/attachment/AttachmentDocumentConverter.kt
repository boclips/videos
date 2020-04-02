package com.boclips.videos.service.infrastructure.attachment

import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import org.bson.types.ObjectId

object AttachmentDocumentConverter {
    fun convert(attachmentDocument: AttachmentDocument): Attachment = Attachment(
        attachmentId = AttachmentId(attachmentDocument.id.toHexString()),
        description = attachmentDocument.description,
        type = AttachmentType.valueOf(attachmentDocument.type),
        linkToResource = attachmentDocument.linkToResource
    )

    fun convert(attachment: Attachment): AttachmentDocument = AttachmentDocument(
        id = ObjectId(attachment.attachmentId.value),
        description = attachment.description,
        type = attachment.type.toString(),
        linkToResource = attachment.linkToResource
    )
}
