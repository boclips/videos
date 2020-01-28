package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.collection.AttachmentResource
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import org.springframework.stereotype.Component

@Component
class AttachmentToResourceConverter(private val attachmentsLinkBuilder: AttachmentsLinkBuilder) {
    fun convert(attachment: Attachment): AttachmentResource {
        return AttachmentResource(
            id = attachment.attachmentId.value,
            description = attachment.description,
            type = attachment.type.name,
            _links = listOfNotNull(attachmentsLinkBuilder.download(attachment)).map { it.rel to it }.toMap()
        )
    }
}