package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.collection.AttachmentResource
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import org.springframework.hateoas.Resource

class AttachmentToResourceConverter(
    private val attachmentsLinkBuilder: AttachmentsLinkBuilder
) {
    fun wrapAttachmentsInResource(attachments: Set<Attachment>): Set<Resource<AttachmentResource>> =
        attachments.map(::convertToResource).toSet()

    private fun convertToResource(attachment: Attachment): Resource<AttachmentResource> = Resource(
        AttachmentResource(
            id = attachment.attachmentId.value,
            description = attachment.description,
            type = attachment.type.name
        ),
        listOfNotNull(attachmentsLinkBuilder.download(attachment))
    )
}
