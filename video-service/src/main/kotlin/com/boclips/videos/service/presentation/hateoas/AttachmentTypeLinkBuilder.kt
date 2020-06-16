package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.AttachmentTypeController
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class AttachmentTypeLinkBuilder {
    fun attachmentTypes(): Link? = getIfHasRole(UserRoles.VIEW_ATTACHMENT_TYPES) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(AttachmentTypeController::class.java).attachmentTypes()
        ).withRel("attachmentTypes")
    }
}
