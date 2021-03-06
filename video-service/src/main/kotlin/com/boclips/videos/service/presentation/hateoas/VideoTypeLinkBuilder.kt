package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.VideoTypeController
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class VideoTypeLinkBuilder {
    fun videoTypes(): Link? = getIfHasRole(UserRoles.VIEW_VIDEO_TYPES) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(VideoTypeController::class.java).videoTypes()
        ).withRel("videoTypes")
    }
}
