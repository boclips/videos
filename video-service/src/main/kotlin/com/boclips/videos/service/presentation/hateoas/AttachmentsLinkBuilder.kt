package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.domain.model.attachment.Attachment
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class AttachmentsLinkBuilder {
    fun download(attachment: Attachment): Link = Link(
        attachment.linkToResource,
        "download"
    )
}
