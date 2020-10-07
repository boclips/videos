package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.domain.model.attachment.Attachment
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class AttachmentsLinkBuilder {
    fun download(attachment: Attachment): HateoasLink {
        return HateoasLink.of(
            Link.of(
                attachment.linkToResource,
                "download"
            )
        )
    }
}
